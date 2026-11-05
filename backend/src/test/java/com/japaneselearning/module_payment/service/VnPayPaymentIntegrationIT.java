package com.japaneselearning.module_payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.entity.OrderItem;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_payment.dto.PaymentCreateReq;
import com.japaneselearning.module_payment.dto.PaymentInitRes;
import com.japaneselearning.module_payment.dto.VnPayIpnResponse;
import com.japaneselearning.module_payment.entity.Payment;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import com.japaneselearning.module_payment.gateway.vnpay.VnPayPaymentGateway;
import com.japaneselearning.module_payment.gateway.vnpay.VnPayProperties;
import com.japaneselearning.module_payment.repository.PaymentRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class VnPayPaymentIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VnPayProperties vnPayProperties;

    private User student;
    private User teacher;
    private Course paidCourse;

    @BeforeEach
    void setUp() {
        student = userRepository.findByEmail("student_vnpay@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_vnpay@example.com")
                        .fullName("Student VNPay")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        teacher = userRepository.findByEmail("teacher_vnpay@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("teacher_vnpay@example.com")
                        .fullName("Teacher VNPay")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        paidCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N1 Master")
                .slug("khoa-hoc-n1-master-" + System.currentTimeMillis())
                .level(CourseLevel.N1)
                .courseType(CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(1200000))
                .salePrice(BigDecimal.valueOf(900000))
                .status(CourseStatus.PUBLISHED)
                .totalStudents(0)
                .build());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(student.getEmail(), "password"));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Order createPendingOrder(BigDecimal amount) {
        Order order = Order.builder()
                .user(student)
                .orderCode("ORD-VNP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase())
                .totalAmount(amount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(amount)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .course(paidCourse)
                .courseTitle(paidCourse.getTitle())
                .price(amount)
                .quantity(1)
                .build();
        order.addItem(item);
        return orderRepository.saveAndFlush(order);
    }

    private Map<String, String> buildSignedVnPayParams(String orderCode, BigDecimal amount, String responseCode, String transactionNo) {
        Map<String, String> params = new HashMap<>();
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountInCents));
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_BankTranNo", "VNP12345678");
        params.put("vnp_CardType", "ATM");
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderCode);
        params.put("vnp_PayDate", "20261105080000");
        params.put("vnp_ResponseCode", responseCode);
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_TransactionNo", transactionNo);
        params.put("vnp_TransactionStatus", "00".equals(responseCode) ? "00" : "02");
        params.put("vnp_TxnRef", orderCode);

        // Sort and build hash
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }

        String secureHash = VnPayPaymentGateway.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        params.put("vnp_SecureHash", secureHash);
        return params;
    }

    @Test
    @DisplayName("Initiate Payment: POST /api/v1/payments/create tạo payment url VNPay hợp lệ cho pending order")
    void initiatePayment_Success() throws Exception {
        Order order = createPendingOrder(BigDecimal.valueOf(900000));

        PaymentCreateReq req = PaymentCreateReq.builder()
                .orderId(order.getId())
                .paymentMethod(PaymentMethod.VNPAY)
                .returnUrl("http://localhost:5173/student/checkout/vnpay-return")
                .build();

        PaymentInitRes initRes = paymentService.createPayment(req);

        assertThat(initRes).isNotNull();
        assertThat(initRes.getOrderId()).isEqualTo(order.getId());
        assertThat(initRes.getPaymentMethod()).isEqualTo(PaymentMethod.VNPAY);
        assertThat(initRes.getPaymentUrl()).contains("vnp_SecureHash=");
        assertThat(initRes.getPaymentUrl()).contains(vnPayProperties.getPayUrl());

        List<Payment> pendingPayments = paymentRepository.findByOrderId(order.getId());
        assertThat(pendingPayments).hasSize(1);
        assertThat(pendingPayments.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("1. Valid Payment Event: Webhook IPN hợp lệ chuyển order sang PAID, kích hoạt Enrollment và tăng totalStudents")
    void vnpayWebhook_ValidPaymentEvent_ActivatesEnrollment() throws Exception {
        Order order = createPendingOrder(BigDecimal.valueOf(900000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(900000), "00", "14123456");

        // Gọi endpoint GET /api/v1/payments/vnpay/ipn
        var requestBuilder = get("/api/v1/payments/vnpay/ipn");
        params.forEach(requestBuilder::param);

        MvcResult result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        VnPayIpnResponse ipnResponse = objectMapper.readValue(result.getResponse().getContentAsString(), VnPayIpnResponse.class);
        assertThat(ipnResponse.getRspCode()).isEqualTo("00");
        assertThat(ipnResponse.getMessage()).isEqualTo("Confirm Success");

        // Kiểm tra Order DB
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        // Kiểm tra Payment DB
        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payments.get(0).getPaymentMethod()).isEqualTo(PaymentMethod.VNPAY);
        assertThat(payments.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(900000));
        assertThat(payments.get(0).getPaidAt()).isNotNull();

        // Kiểm tra Enrollment DB
        Optional<CourseEnrollment> enrollmentOpt = enrollmentRepository.findByUserIdAndCourseId(student.getId(), paidCourse.getId());
        assertThat(enrollmentOpt).isPresent();
        assertThat(enrollmentOpt.get().getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);

        // Kiểm tra Course totalStudents tăng 1
        Course updatedCourse = courseRepository.findById(paidCourse.getId()).orElseThrow();
        assertThat(updatedCourse.getTotalStudents()).isEqualTo(1);
    }

    @Test
    @DisplayName("2. Duplicate Payment Event: Webhook trùng lặp trả về 02 (Order already confirmed), không tạo enrollment hoặc tăng count lần 2")
    void vnpayWebhook_DuplicatePaymentEvent_IsIdempotent() throws Exception {
        Order order = createPendingOrder(BigDecimal.valueOf(900000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(900000), "00", "14123456");

        // Lần 1: Thành công
        VnPayIpnResponse firstResponse = paymentService.handleVnPayIpn(params);
        assertThat(firstResponse.getRspCode()).isEqualTo("00");

        Order orderAfterFirst = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(orderAfterFirst.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(courseRepository.findById(paidCourse.getId()).orElseThrow().getTotalStudents()).isEqualTo(1);

        // Lần 2: Gửi lại chính xác webhook đó
        VnPayIpnResponse secondResponse = paymentService.handleVnPayIpn(params);
        assertThat(secondResponse.getRspCode()).isEqualTo("02");
        assertThat(secondResponse.getMessage()).contains("already confirmed");

        // Kiểm tra DB sau lần 2: Không nhân đôi payment record, không nhân đôi enrollment, không tăng count
        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        assertThat(payments).hasSize(1);

        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(student.getId());
        assertThat(enrollments).hasSize(1);

        Course courseAfterSecond = courseRepository.findById(paidCourse.getId()).orElseThrow();
        assertThat(courseAfterSecond.getTotalStudents()).isEqualTo(1);
    }

    @Test
    @DisplayName("3. Failed Payment Event: Khách hủy giao dịch (code 24) -> Order chuyển sang CANCELLED, Payment lưu FAILED, không kích hoạt enrollment")
    void vnpayWebhook_CancelledPaymentEvent() {
        Order order = createPendingOrder(BigDecimal.valueOf(900000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(900000), "24", "14123457");

        VnPayIpnResponse response = paymentService.handleVnPayIpn(params);
        assertThat(response.getRspCode()).isEqualTo("00");

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.CANCELLED);

        // Enrollment tuyệt đối không được tạo
        assertThat(enrollmentRepository.findByUserIdAndCourseId(student.getId(), paidCourse.getId())).isEmpty();
        assertThat(courseRepository.findById(paidCourse.getId()).orElseThrow().getTotalStudents()).isEqualTo(0);
    }

    @Test
    @DisplayName("4. Expired Payment Event: Giao dịch hết hạn (code 11) -> Order chuyển sang EXPIRED, Payment FAILED, không kích hoạt enrollment")
    void vnpayWebhook_ExpiredPaymentEvent() {
        Order order = createPendingOrder(BigDecimal.valueOf(900000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(900000), "11", "14123458");

        VnPayIpnResponse response = paymentService.handleVnPayIpn(params);
        assertThat(response.getRspCode()).isEqualTo("00");

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.EXPIRED);

        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.FAILED);

        assertThat(enrollmentRepository.findByUserIdAndCourseId(student.getId(), paidCourse.getId())).isEmpty();
        assertThat(courseRepository.findById(paidCourse.getId()).orElseThrow().getTotalStudents()).isEqualTo(0);
    }

    @Test
    @DisplayName("5. Forged Payment Event - Invalid Checksum: Chữ ký HMAC sai lệch bị từ chối với mã 97, Order giữ nguyên PENDING")
    void vnpayWebhook_ForgedChecksum_Rejected() {
        Order order = createPendingOrder(BigDecimal.valueOf(900000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(900000), "00", "14123459");

        // Giả mạo chữ ký
        params.put("vnp_SecureHash", "INVALID_FORGED_HASH_1234567890ABCDEF");

        VnPayIpnResponse response = paymentService.handleVnPayIpn(params);
        assertThat(response.getRspCode()).isEqualTo("97");
        assertThat(response.getMessage()).isEqualTo("Invalid Checksum");

        // Order giữ nguyên PENDING, không có payment hay enrollment
        Order orderInDb = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(orderInDb.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(paymentRepository.findByOrderId(order.getId())).isEmpty();
        assertThat(enrollmentRepository.findByUserIdAndCourseId(student.getId(), paidCourse.getId())).isEmpty();
    }

    @Test
    @DisplayName("5b. Forged Payment Event - Amount Tampering: Số tiền vnp_Amount không khớp với đơn hàng bị từ chối với mã 04")
    void vnpayWebhook_AmountTampering_Rejected() {
        Order order = createPendingOrder(BigDecimal.valueOf(900000));

        // Kẻ tấn công sửa vnp_Amount thành 100,000 VND thay vì 900,000 VND và ký lại bằng hashSecret đúng
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(100000), "00", "14123460");

        VnPayIpnResponse response = paymentService.handleVnPayIpn(params);
        assertThat(response.getRspCode()).isEqualTo("04");
        assertThat(response.getMessage()).isEqualTo("Invalid Amount");

        // Order giữ nguyên PENDING, không cho phép kích hoạt
        Order orderInDb = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(orderInDb.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(enrollmentRepository.findByUserIdAndCourseId(student.getId(), paidCourse.getId())).isEmpty();
    }

    @Test
    @DisplayName("Late Failed Event after PAID: Webhook thất bại đến muộn không được ghi đè đơn hàng đã PAID")
    void vnpayWebhook_LateFailedEvent_DoesNotRevertPaidOrder() {
        Order order = createPendingOrder(BigDecimal.valueOf(900000));
        Map<String, String> successParams = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(900000), "00", "14123461");

        // Đơn hàng thanh toán thành công trước
        paymentService.handleVnPayIpn(successParams);
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);

        // Sau đó nhận một webhook trễ báo thất bại
        Map<String, String> lateFailedParams = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(900000), "24", "14123462");
        VnPayIpnResponse response = paymentService.handleVnPayIpn(lateFailedParams);

        assertThat(response.getRspCode()).isEqualTo("02");
        // Đơn hàng vẫn duy trì trạng thái PAID
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
    }
}
