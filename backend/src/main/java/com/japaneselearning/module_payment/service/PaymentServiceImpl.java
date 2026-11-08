package com.japaneselearning.module_payment.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.observability.ObservabilityMetrics;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.policy.EnrollmentAccessPolicy;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.entity.OrderItem;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_payment.dto.*;
import com.japaneselearning.module_payment.entity.Payment;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import com.japaneselearning.module_payment.gateway.*;
import com.japaneselearning.module_payment.repository.PaymentRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ObservabilityMetrics observabilityMetrics;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final Environment environment;

    @Value("${app.payment.simulation-enabled:false}")
    private boolean simulationEnabled;

    @Override
    @Transactional
    public PaymentInitRes createPayment(PaymentCreateReq req) {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findByIdForUpdate(req.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() == OrderStatus.PAID) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATUS);
        }

        if (req.getPaymentMethod() == PaymentMethod.SIMULATION && (isProductionProfile() || !simulationEnabled)) {
            throw new AppException(ErrorCode.PAYMENT_SIMULATION_DISABLED);
        }

        PaymentGateway gateway = gatewayRegistry.getGateway(req.getPaymentMethod());

        PaymentInitCommand command = PaymentInitCommand.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .amount(order.getFinalAmount())
                .orderInfo("Thanh toan don hang " + order.getOrderCode())
                .clientIp("127.0.0.1")
                .returnUrl(req.getReturnUrl())
                .build();

        PaymentInitResult initResult = gateway.initPayment(command);

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(req.getPaymentMethod())
                .paymentProvider(initResult.getPaymentProvider())
                .transactionCode(initResult.getTransactionCode())
                .amount(order.getFinalAmount())
                .status(PaymentStatus.PENDING)
                .rawResponse("Payment initiated via " + initResult.getPaymentProvider())
                .build();

        paymentRepository.save(payment);

        return PaymentInitRes.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .paymentUrl(initResult.getPaymentUrl())
                .transactionCode(initResult.getTransactionCode())
                .paymentMethod(req.getPaymentMethod())
                .paymentProvider(initResult.getPaymentProvider())
                .amount(order.getFinalAmount())
                .build();
    }

    @Override
    @Transactional
    public VnPayIpnResponse handleVnPayIpn(Map<String, String> allParams) {
        PaymentGateway gateway = gatewayRegistry.getGateway(PaymentMethod.VNPAY);
        PaymentWebhookPayload payload = PaymentWebhookPayload.builder()
                .params(allParams)
                .build();

        PaymentWebhookResult webhookResult = gateway.processWebhook(payload);

        if (!webhookResult.isSignatureValid()) {
            log.warn("VNPay IPN invalid checksum for params: {}", allParams);
            return new VnPayIpnResponse("97", "Invalid Checksum");
        }

        String orderCode = webhookResult.getOrderCode();
        if (orderCode == null || orderCode.isBlank()) {
            log.warn("VNPay IPN missing vnp_TxnRef");
            return new VnPayIpnResponse("01", "Order not found");
        }

        Optional<Order> orderOpt = orderRepository.findByOrderCodeForUpdate(orderCode);
        if (orderOpt.isEmpty()) {
            log.warn("VNPay IPN order not found for code: {}", orderCode);
            return new VnPayIpnResponse("01", "Order not found");
        }

        Order order = orderOpt.get();

        // Amount verification: compare webhook amount with order amount
        if (webhookResult.getAmount() != null &&
                webhookResult.getAmount().compareTo(order.getFinalAmount()) != 0) {
            log.error("VNPay IPN amount mismatch: expected {}, received {}",
                    order.getFinalAmount(), webhookResult.getAmount());
            return new VnPayIpnResponse("04", "Invalid Amount");
        }

        // Idempotency check: if order is already PAID, return success without re-processing
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("VNPay IPN duplicate event for order {} which is already PAID. Acknowledging idempotently.", orderCode);
            return new VnPayIpnResponse("02", "Order already confirmed");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("VNPay IPN received for non-pending order {} (status: {})", orderCode, order.getStatus());
            return new VnPayIpnResponse("02", "Order already closed");
        }

        // Process based on event status
        if (webhookResult.getEventStatus() == PaymentEventStatus.SUCCESS) {
            order.transitionTo(OrderStatus.PAID);
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.VNPAY)
                    .paymentProvider("VNPAY")
                    .transactionCode(webhookResult.getTransactionCode())
                    .amount(order.getFinalAmount())
                    .status(PaymentStatus.SUCCESS)
                    .paidAt(LocalDateTime.now())
                    .rawResponse(webhookResult.getRawResponse())
                    .build();
            paymentRepository.save(payment);

            activateEnrollmentsForOrder(order, order.getUser());

            observabilityMetrics.recordOrderProcessed(
                    OrderStatus.PAID.name(),
                    PaymentMethod.VNPAY.name()
            );

            log.info("VNPay payment successfully processed and active enrollments created for order {}", orderCode);
            return new VnPayIpnResponse("00", "Confirm Success");

        } else if (webhookResult.getEventStatus() == PaymentEventStatus.CANCELLED) {
            order.transitionTo(OrderStatus.CANCELLED);
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.VNPAY)
                    .paymentProvider("VNPAY")
                    .transactionCode(webhookResult.getTransactionCode())
                    .amount(order.getFinalAmount())
                    .status(PaymentStatus.CANCELLED)
                    .rawResponse(webhookResult.getRawResponse())
                    .build();
            paymentRepository.save(payment);

            log.info("VNPay payment cancelled for order {}", orderCode);
            return new VnPayIpnResponse("00", "Confirm Success");

        } else if (webhookResult.getEventStatus() == PaymentEventStatus.EXPIRED) {
            order.transitionTo(OrderStatus.EXPIRED);
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.VNPAY)
                    .paymentProvider("VNPAY")
                    .transactionCode(webhookResult.getTransactionCode())
                    .amount(order.getFinalAmount())
                    .status(PaymentStatus.FAILED)
                    .rawResponse(webhookResult.getRawResponse())
                    .build();
            paymentRepository.save(payment);

            log.info("VNPay payment expired for order {}", orderCode);
            return new VnPayIpnResponse("00", "Confirm Success");

        } else {
            order.transitionTo(OrderStatus.FAILED);
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.VNPAY)
                    .paymentProvider("VNPAY")
                    .transactionCode(webhookResult.getTransactionCode())
                    .amount(order.getFinalAmount())
                    .status(PaymentStatus.FAILED)
                    .rawResponse(webhookResult.getRawResponse())
                    .build();
            paymentRepository.save(payment);

            log.info("VNPay payment failed for order {} with code {}", orderCode, webhookResult.getResponseCode());
            return new VnPayIpnResponse("00", "Confirm Success");
        }
    }

    @Override
    @Transactional
    public PaymentRes handleGenericWebhook(PaymentMethod method, Map<String, String> params, String rawBody, String signature) {
        PaymentGateway gateway = gatewayRegistry.getGateway(method);
        PaymentWebhookPayload payload = PaymentWebhookPayload.builder()
                .params(params)
                .rawBody(rawBody)
                .signatureHeader(signature)
                .build();

        PaymentWebhookResult webhookResult = gateway.processWebhook(payload);

        if (!webhookResult.isSignatureValid()) {
            throw new AppException(ErrorCode.PAYMENT_SIGNATURE_INVALID);
        }

        String orderCode = webhookResult.getOrderCode();
        Order order = orderRepository.findByOrderCodeForUpdate(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.PAID) {
            Payment existingPayment = paymentRepository
                    .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.SUCCESS)
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
            return mapToPaymentRes(existingPayment, order);
        }

        if (webhookResult.getEventStatus() == PaymentEventStatus.SUCCESS) {
            order.transitionTo(OrderStatus.PAID);
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(method)
                    .paymentProvider(method.name())
                    .transactionCode(webhookResult.getTransactionCode())
                    .amount(order.getFinalAmount())
                    .status(PaymentStatus.SUCCESS)
                    .paidAt(LocalDateTime.now())
                    .rawResponse(webhookResult.getRawResponse())
                    .build();
            payment = paymentRepository.save(payment);

            activateEnrollmentsForOrder(order, order.getUser());

            observabilityMetrics.recordOrderProcessed(
                    OrderStatus.PAID.name(),
                    method.name()
            );

            return mapToPaymentRes(payment, order);
        } else {
            OrderStatus targetStatus = webhookResult.getEventStatus() == PaymentEventStatus.EXPIRED
                    ? OrderStatus.EXPIRED
                    : OrderStatus.FAILED;
            order.transitionTo(targetStatus);
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(method)
                    .paymentProvider(method.name())
                    .transactionCode(webhookResult.getTransactionCode())
                    .amount(order.getFinalAmount())
                    .status(PaymentStatus.FAILED)
                    .rawResponse(webhookResult.getRawResponse())
                    .build();
            payment = paymentRepository.save(payment);

            return mapToPaymentRes(payment, order);
        }
    }

    @Override
    @Transactional
    public PaymentRes simulatePayment(Long orderId) {
        if (isProductionProfile() || !simulationEnabled) {
            throw new AppException(ErrorCode.PAYMENT_SIMULATION_DISABLED);
        }

        User currentUser = getCurrentUser();

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() == OrderStatus.PAID) {
            Payment existingPayment = paymentRepository
                    .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.SUCCESS)
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
            return mapToPaymentRes(existingPayment, order);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATUS);
        }

        String transactionCode = "SIM-" + order.getOrderCode() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(PaymentMethod.SIMULATION)
                .paymentProvider("LOCAL_SIMULATION")
                .transactionCode(transactionCode)
                .amount(order.getFinalAmount())
                .status(PaymentStatus.SUCCESS)
                .paidAt(now)
                .rawResponse("Local payment simulation successful for order " + order.getOrderCode())
                .build();

        payment = paymentRepository.save(payment);

        order.transitionTo(OrderStatus.PAID);
        orderRepository.save(order);

        // Record order processing metric
        observabilityMetrics.recordOrderProcessed(
                OrderStatus.PAID.name(),
                payment.getPaymentMethod().name()
        );

        activateEnrollmentsForOrder(order, currentUser);

        return mapToPaymentRes(payment, order);
    }

    private void activateEnrollmentsForOrder(Order order, User user) {
        if (order.getItems() == null) {
            return;
        }

        for (OrderItem item : order.getItems()) {
            Course course = item.getCourse();
            if (course == null) {
                continue;
            }

            Optional<CourseEnrollment> existingOpt = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId());
            if (existingOpt.isPresent()) {
                CourseEnrollment existing = existingOpt.get();
                if (!EnrollmentAccessPolicy.hasAccess(existing.getStatus())) {
                    existing.setStatus(EnrollmentStatus.ACTIVE);
                    enrollmentRepository.save(existing);
                    courseRepository.incrementTotalStudents(course.getId());
                }
            } else {
                CourseEnrollment newEnrollment = CourseEnrollment.builder()
                        .user(user)
                        .course(course)
                        .status(EnrollmentStatus.ACTIVE)
                        .progressPercent(0)
                        .build();
                enrollmentRepository.save(newEnrollment);
                courseRepository.incrementTotalStudents(course.getId());
            }
        }
    }

    private boolean isProductionProfile() {
        if (environment == null) {
            return false;
        }
        return Arrays.asList(environment.getActiveProfiles()).contains("prod") ||
               environment.matchesProfiles("prod");
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private PaymentRes mapToPaymentRes(Payment payment, Order order) {
        return PaymentRes.builder()
                .id(payment.getId())
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .paymentMethod(payment.getPaymentMethod())
                .paymentProvider(payment.getPaymentProvider())
                .transactionCode(payment.getTransactionCode())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .orderStatus(order.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
