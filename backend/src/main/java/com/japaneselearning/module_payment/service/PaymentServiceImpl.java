package com.japaneselearning.module_payment.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.observability.ObservabilityMetrics;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.entity.OrderItem;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_payment.dto.PaymentRes;
import com.japaneselearning.module_payment.entity.Payment;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import com.japaneselearning.module_payment.repository.PaymentRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ObservabilityMetrics observabilityMetrics;

    @Value("${app.payment.simulation-enabled:false}")
    private boolean simulationEnabled;

    @Override
    @Transactional
    public PaymentRes simulatePayment(Long orderId) {
        if (!simulationEnabled) {
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

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Record order processing metric
        observabilityMetrics.recordOrderProcessed(
                OrderStatus.PAID.name(),
                payment.getPaymentMethod().name()
        );

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Course course = item.getCourse();
                if (course == null) {
                    continue;
                }

                Optional<CourseEnrollment> existingOpt = enrollmentRepository.findByUserIdAndCourseId(currentUser.getId(), course.getId());
                if (existingOpt.isPresent()) {
                    CourseEnrollment existing = existingOpt.get();
                    if (existing.getStatus() != EnrollmentStatus.ACTIVE) {
                        existing.setStatus(EnrollmentStatus.ACTIVE);
                        enrollmentRepository.save(existing);
                        courseRepository.incrementTotalStudents(course.getId());
                    }
                } else {
                    CourseEnrollment newEnrollment = CourseEnrollment.builder()
                            .user(currentUser)
                            .course(course)
                            .status(EnrollmentStatus.ACTIVE)
                            .progressPercent(0)
                            .build();
                    enrollmentRepository.save(newEnrollment);
                    courseRepository.incrementTotalStudents(course.getId());
                }
            }
        }

        return mapToPaymentRes(payment, order);
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
