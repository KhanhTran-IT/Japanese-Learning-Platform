package com.japaneselearning.module_payment.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.audit.AuditAction;
import com.japaneselearning.common.audit.AuditLog;
import com.japaneselearning.common.audit.AuditLogRepository;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.entity.OrderItem;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_payment.dto.VnPayIpnResponse;
import com.japaneselearning.module_payment.entity.Payment;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import com.japaneselearning.module_payment.gateway.vnpay.VnPayPaymentGateway;
import com.japaneselearning.module_payment.gateway.vnpay.VnPayProperties;
import com.japaneselearning.module_payment.policy.PaymentRetentionPolicy;
import com.japaneselearning.module_payment.repository.PaymentRepository;
import com.japaneselearning.module_payment.scheduler.PaymentRetentionScheduler;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentSecurityAndRedactionIT {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRetentionScheduler retentionScheduler;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private VnPayProperties vnPayProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = userRepository.findByEmail("security_student@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("security_student@example.com")
                        .fullName("Security Student")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        course = courseRepository.save(Course.builder()
                .teacher(student)
                .title("Security Test Course")
                .slug("security-course-" + System.currentTimeMillis())
                .level(CourseLevel.N3)
                .courseType(CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(1000000))
                .salePrice(BigDecimal.valueOf(750000))
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
                .orderCode("ORD-SEC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase())
                .totalAmount(amount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(amount)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .course(course)
                .courseTitle(course.getTitle())
                .price(amount)
                .quantity(1)
                .build();
        order.addItem(item);
        return orderRepository.saveAndFlush(order);
    }

    private Map<String, String> buildSignedVnPayParams(String orderCode, BigDecimal amount, String responseCode, String transactionNo, String clientIp) {
        Map<String, String> params = new HashMap<>();
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountInCents));
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_BankTranNo", "VNP99887766");
        params.put("vnp_CardType", "ATM");
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderCode);
        params.put("vnp_PayDate", "20261118120000");
        params.put("vnp_ResponseCode", responseCode);
        params.put("vnp_TransactionNo", transactionNo);
        params.put("vnp_TransactionStatus", "00".equals(responseCode) ? "00" : "02");
        params.put("vnp_TxnRef", orderCode);
        params.put("vnp_IpAddr", clientIp); // Client personal IP address

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (!first) {
                    hashData.append('&');
                }
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                first = false;
            }
        }

        String secureHash = VnPayPaymentGateway.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        params.put("vnp_SecureHash", secureHash);
        params.put("vnp_SecureHashType", "HMACSHA512");
        return params;
    }

    @Test
    @DisplayName("Audit 1: Logs must NEVER contain raw parameter maps, secure hashes, or client IPs on invalid checksum")
    void handleVnPayIpn_InvalidChecksum_LogsDoNotContainSensitiveFields() {
        Logger paymentServiceLogger = (Logger) LoggerFactory.getLogger(PaymentServiceImpl.class);
        Logger gatewayLogger = (Logger) LoggerFactory.getLogger(VnPayPaymentGateway.class);

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        paymentServiceLogger.addAppender(appender);
        gatewayLogger.addAppender(appender);

        String rawClientIp = "192.168.100.45";
        String fakeHash = "BADC0DE123456789ABCDEF0123456789DEADBEEF";
        Order order = createPendingOrder(BigDecimal.valueOf(750000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(750000), "00", "778899", rawClientIp);
        params.put("vnp_SecureHash", fakeHash); // Tamper hash

        try {
            VnPayIpnResponse response = paymentService.handleVnPayIpn(params);
            assertThat(response.getRspCode()).isEqualTo("97");

            List<String> loggedMessages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .toList();

            for (String msg : loggedMessages) {
                // Must not leak the secure hash value or parameter map
                assertThat(msg).doesNotContain(fakeHash);
                // Must not leak client IP
                assertThat(msg).doesNotContain(rawClientIp);
                // Must not dump the parameter map toString
                assertThat(msg).doesNotContain("vnp_SecureHash=");
                assertThat(msg).doesNotContain("vnp_IpAddr=");
            }
        } finally {
            paymentServiceLogger.detachAppender(appender);
            gatewayLogger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("Audit 2: Persisted Payment.rawResponse contains ONLY reconciliation metadata and is free of sensitive fields")
    void handleVnPayIpn_Success_RawResponseIsSanitizedAndFreeOfSensitiveFields() throws Exception {
        String clientIp = "10.20.30.40";
        Order order = createPendingOrder(BigDecimal.valueOf(750000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(750000), "00", "12345678", clientIp);
        String actualHash = params.get("vnp_SecureHash");

        VnPayIpnResponse response = paymentService.handleVnPayIpn(params);
        assertThat(response.getRspCode()).isEqualTo("00");

        List<Payment> payments = paymentRepository.findByOrderId(order.getId());
        assertThat(payments).hasSize(1);
        Payment payment = payments.get(0);
        String rawResponse = payment.getRawResponse();

        assertThat(rawResponse).isNotNull();
        // 1. Must NOT contain sensitive hash or hash value
        assertThat(rawResponse).doesNotContain(actualHash);
        assertThat(rawResponse).doesNotContain("vnp_SecureHash");
        assertThat(rawResponse).doesNotContain("vnp_SecureHashType");

        // 2. Must NOT contain merchant credentials or client IP
        assertThat(rawResponse).doesNotContain(vnPayProperties.getHashSecret());
        assertThat(rawResponse).doesNotContain(clientIp);
        assertThat(rawResponse).doesNotContain("vnp_IpAddr");
        assertThat(rawResponse).doesNotContain("vnp_TmnCode");

        // 3. Must be valid JSON containing reconciliation metadata
        JsonNode jsonNode = objectMapper.readTree(rawResponse);
        assertThat(jsonNode.path("vnp_TxnRef").asText()).isEqualTo(order.getOrderCode());
        assertThat(jsonNode.path("vnp_TransactionNo").asText()).isEqualTo("12345678");
        assertThat(jsonNode.path("vnp_Amount").asText()).isEqualTo("75000000");
        assertThat(jsonNode.path("vnp_BankCode").asText()).isEqualTo("NCB");
        assertThat(jsonNode.path("vnp_BankTranNo").asText()).isEqualTo("VNP99887766");
        assertThat(jsonNode.path("vnp_CardType").asText()).isEqualTo("ATM");
        assertThat(jsonNode.path("vnp_ResponseCode").asText()).isEqualTo("00");
        assertThat(jsonNode.path("vnp_TransactionStatus").asText()).isEqualTo("00");
    }

    @Test
    @DisplayName("Audit 3: Audit metadata is strictly free from secure hashes, credentials, and client IPs")
    void handleVnPayIpn_AuditMetadata_IsFreeOfSensitiveFields() {
        Order order = createPendingOrder(BigDecimal.valueOf(750000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(750000), "00", "88990011", "172.16.0.5");
        String actualHash = params.get("vnp_SecureHash");

        paymentService.handleVnPayIpn(params);

        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertThat(auditLogs).isNotEmpty();

        for (AuditLog logEntry : auditLogs) {
            String metadata = logEntry.getMetadata();
            if (metadata != null) {
                assertThat(metadata).doesNotContain(actualHash);
                assertThat(metadata).doesNotContain("vnp_SecureHash");
                assertThat(metadata).doesNotContain("vnp_SecureHashType");
                assertThat(metadata).doesNotContain(vnPayProperties.getHashSecret());
                assertThat(metadata).doesNotContain("172.16.0.5");
                assertThat(metadata).doesNotContain("vnp_IpAddr");
                assertThat(metadata).doesNotContain("vnp_TmnCode");
            }
        }
    }

    @Test
    @DisplayName("Audit 4: Retention Policy purges raw responses older than threshold while preserving financial transaction records")
    void retentionPolicy_PurgesExpiredRawResponses_PreservingFinancialRecords() {
        Order orderOld = createPendingOrder(BigDecimal.valueOf(500000));
        orderOld.setStatus(OrderStatus.PAID);
        orderRepository.saveAndFlush(orderOld);

        // Payment created 100 days ago
        Payment oldPayment = Payment.builder()
                .order(orderOld)
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentProvider("VNPAY")
                .transactionCode("VNP-OLD-100DAYS")
                .amount(BigDecimal.valueOf(500000))
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now().minusDays(100))
                .rawResponse("{\"vnp_TxnRef\":\"OLD\",\"vnp_TransactionNo\":\"1001\"}")
                .createdAt(LocalDateTime.now().minusDays(100))
                .build();
        oldPayment = paymentRepository.saveAndFlush(oldPayment);
        jdbcTemplate.update("UPDATE payments SET created_at = ? WHERE id = ?", LocalDateTime.now().minusDays(100), oldPayment.getId());

        Order orderRecent = createPendingOrder(BigDecimal.valueOf(300000));
        orderRecent.setStatus(OrderStatus.PAID);
        orderRepository.saveAndFlush(orderRecent);

        // Payment created 10 days ago (within 90-day retention)
        Payment recentPayment = Payment.builder()
                .order(orderRecent)
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentProvider("VNPAY")
                .transactionCode("VNP-RECENT-10DAYS")
                .amount(BigDecimal.valueOf(300000))
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now().minusDays(10))
                .rawResponse("{\"vnp_TxnRef\":\"RECENT\",\"vnp_TransactionNo\":\"2002\"}")
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();
        recentPayment = paymentRepository.saveAndFlush(recentPayment);
        jdbcTemplate.update("UPDATE payments SET created_at = ? WHERE id = ?", LocalDateTime.now().minusDays(10), recentPayment.getId());

        // Execute retention cleanup (90 days)
        int purgedCount = paymentService.purgeExpiredPaymentPayloads(90);
        assertThat(purgedCount).isGreaterThanOrEqualTo(1);

        Payment reloadedOld = paymentRepository.findById(oldPayment.getId()).orElseThrow();
        // Raw response must be purged
        assertThat(reloadedOld.getRawResponse()).isEqualTo(PaymentRetentionPolicy.PURGED_PAYLOAD_MARKER);
        // Financial records must remain completely intact
        assertThat(reloadedOld.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500000));
        assertThat(reloadedOld.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(reloadedOld.getTransactionCode()).isEqualTo("VNP-OLD-100DAYS");
        assertThat(reloadedOld.getPaidAt()).isNotNull();

        Payment reloadedRecent = paymentRepository.findById(recentPayment.getId()).orElseThrow();
        // Recent payment retains raw response for ongoing reconciliation
        assertThat(reloadedRecent.getRawResponse()).contains("RECENT");
        assertThat(reloadedRecent.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(300000));
    }

    @Test
    @DisplayName("Audit 5: Payment idempotency and signature verification are completely preserved")
    void paymentFlow_SignatureVerificationAndIdempotencyPreserved() {
        Order order = createPendingOrder(BigDecimal.valueOf(750000));
        Map<String, String> params = buildSignedVnPayParams(order.getOrderCode(), BigDecimal.valueOf(750000), "00", "99001122", "127.0.0.1");

        // 1. First run: signature valid, processes payment to SUCCESS and order to PAID
        VnPayIpnResponse firstResponse = paymentService.handleVnPayIpn(params);
        assertThat(firstResponse.getRspCode()).isEqualTo("00");
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);

        // 2. Second run: duplicate webhook returns 02 idempotently without duplicate payment
        VnPayIpnResponse secondResponse = paymentService.handleVnPayIpn(params);
        assertThat(secondResponse.getRspCode()).isEqualTo("02");
        assertThat(paymentRepository.findByOrderId(order.getId())).hasSize(1);

        // 3. Invalid signature: rejected with 97
        params.put("vnp_SecureHash", "INVALID_CHECKSUM_HASH");
        VnPayIpnResponse forgedResponse = paymentService.handleVnPayIpn(params);
        assertThat(forgedResponse.getRspCode()).isEqualTo("97");
    }
}
