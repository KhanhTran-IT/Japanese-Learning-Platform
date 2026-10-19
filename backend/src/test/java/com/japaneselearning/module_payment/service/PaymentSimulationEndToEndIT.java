package com.japaneselearning.module_payment.service;

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
import com.japaneselearning.module_payment.dto.PaymentRes;
import com.japaneselearning.module_payment.entity.Payment;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import com.japaneselearning.module_payment.repository.PaymentRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentSimulationEndToEndIT {

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

    private User student;
    private User teacher;
    private Course paidCourse;

    @BeforeEach
    void setUp() {
        student = userRepository.findByEmail("student_e2e@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_e2e@example.com")
                        .fullName("Student E2E")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        teacher = userRepository.findByEmail("teacher_e2e@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("teacher_e2e@example.com")
                        .fullName("Teacher E2E")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        paidCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N2 Chuyên Sâu")
                .slug("khoa-hoc-n2-chuyen-sau")
                .level(CourseLevel.N2)
                .courseType(CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(1000000))
                .salePrice(BigDecimal.valueOf(800000))
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

    @Test
    @DisplayName("End-to-End: Thanh toán và hoàn tất ghi danh trọn vẹn trong DB, kiểm tra tính idempotent")
    void simulatePayment_FullFlowAndIdempotency() {
        // 1. Tạo order PENDING
        Order order = Order.builder()
                .user(student)
                .orderCode("ORD-E2E-" + System.currentTimeMillis())
                .totalAmount(BigDecimal.valueOf(800000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(800000))
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        OrderItem item = OrderItem.builder()
                .course(paidCourse)
                .courseTitle(paidCourse.getTitle())
                .price(BigDecimal.valueOf(800000))
                .quantity(1)
                .build();
        order.addItem(item);
        Order savedOrder = orderRepository.saveAndFlush(order);

        // 2. Chạy simulate payment lần 1
        PaymentRes firstRes = paymentService.simulatePayment(savedOrder.getId());

        assertThat(firstRes).isNotNull();
        assertThat(firstRes.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(firstRes.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(firstRes.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(800000));
        assertThat(firstRes.getPaymentMethod()).isEqualTo(PaymentMethod.SIMULATION);

        // Kiểm tra trạng thái DB sau lần 1
        Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        List<Payment> payments = paymentRepository.findByOrderId(savedOrder.getId());
        assertThat(payments).hasSize(1);
        Payment payment = payments.get(0);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(800000));

        CourseEnrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(student.getId(), paidCourse.getId()).orElseThrow();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);

        Course updatedCourse = courseRepository.findById(paidCourse.getId()).orElseThrow();
        assertThat(updatedCourse.getTotalStudents()).isEqualTo(1);

        // 3. Chạy simulate payment lần 2 (Idempotency check)
        PaymentRes secondRes = paymentService.simulatePayment(savedOrder.getId());

        assertThat(secondRes).isNotNull();
        assertThat(secondRes.getId()).isEqualTo(firstRes.getId());
        assertThat(secondRes.getTransactionCode()).isEqualTo(firstRes.getTransactionCode());

        // Kiểm tra DB sau lần 2: Không tạo thêm record thanh toán hay enrollment nào, counter không tăng lần 2
        List<Payment> paymentsAfterRetry = paymentRepository.findByOrderId(savedOrder.getId());
        assertThat(paymentsAfterRetry).hasSize(1);

        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(student.getId());
        assertThat(enrollments).hasSize(1);

        Course courseAfterRetry = courseRepository.findById(paidCourse.getId()).orElseThrow();
        assertThat(courseAfterRetry.getTotalStudents()).isEqualTo(1);
    }
}
