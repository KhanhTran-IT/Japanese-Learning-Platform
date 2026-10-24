package com.japaneselearning.module_payment.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.japaneselearning.common.observability.ObservabilityMetrics;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObservabilityMetrics observabilityMetrics;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User student;
    private User otherStudent;
    private Course paidCourse;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "simulationEnabled", true);

        student = User.builder().id(1L).email("student@example.com").build();
        otherStudent = User.builder().id(2L).email("other@example.com").build();

        paidCourse = Course.builder()
                .id(10L)
                .title("Khóa học N3 Cấp Tốc")
                .totalStudents(5)
                .build();

        OrderItem item = OrderItem.builder()
                .id(100L)
                .course(paidCourse)
                .courseTitle(paidCourse.getTitle())
                .price(BigDecimal.valueOf(500000))
                .quantity(1)
                .build();

        pendingOrder = Order.builder()
                .id(1000L)
                .orderCode("ORD-20261019-123456")
                .user(student)
                .totalAmount(BigDecimal.valueOf(500000))
                .finalAmount(BigDecimal.valueOf(500000))
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>(List.of(item)))
                .build();
        item.setOrder(pendingOrder);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("student@example.com", "password"));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("simulatePayment: Thành công khi thanh toán order PENDING, tạo payment, set PAID, ghi danh và tăng totalStudents")
    void simulatePayment_Success() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(orderRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(pendingOrder));
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 10L)).thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(500L);
            p.setCreatedAt(LocalDateTime.now());
            return p;
        });

        PaymentRes result = paymentService.simulatePayment(1000L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(500L);
        assertThat(result.getOrderId()).isEqualTo(1000L);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500000));
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.SIMULATION);
        assertThat(result.getTransactionCode()).startsWith("SIM-ORD-20261019-123456-");

        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(pendingOrder);
        verify(enrollmentRepository).save(any(CourseEnrollment.class));
        verify(courseRepository).incrementTotalStudents(10L);
    }

    @Test
    @DisplayName("simulatePayment: Kích hoạt lại enrollment bị CANCELLED thành ACTIVE và tăng totalStudents")
    void simulatePayment_ReactivatesCancelledEnrollment() {
        CourseEnrollment cancelledEnrollment = CourseEnrollment.builder()
                .id(200L)
                .user(student)
                .course(paidCourse)
                .status(EnrollmentStatus.CANCELLED)
                .progressPercent(20)
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(orderRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(pendingOrder));
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 10L)).thenReturn(Optional.of(cancelledEnrollment));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(501L);
            return p;
        });

        PaymentRes result = paymentService.simulatePayment(1000L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(cancelledEnrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        verify(enrollmentRepository).save(cancelledEnrollment);
        verify(courseRepository).incrementTotalStudents(10L);
    }

    @Test
    @DisplayName("simulatePayment: Idempotent retry khi order đã PAID không tạo duplicate payment hay tăng counter")
    void simulatePayment_IdempotentRetry_AlreadyPaid() {
        pendingOrder.setStatus(OrderStatus.PAID);

        Payment existingPayment = Payment.builder()
                .id(502L)
                .order(pendingOrder)
                .paymentMethod(PaymentMethod.SIMULATION)
                .paymentProvider("LOCAL_SIMULATION")
                .transactionCode("SIM-ORD-20261019-123456-EXISTING")
                .amount(BigDecimal.valueOf(500000))
                .status(PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now().minusMinutes(5))
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(orderRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(pendingOrder));
        when(paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(1000L, PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(existingPayment));

        PaymentRes result = paymentService.simulatePayment(1000L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(502L);
        assertThat(result.getTransactionCode()).isEqualTo("SIM-ORD-20261019-123456-EXISTING");
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        verify(paymentRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
        verify(courseRepository, never()).incrementTotalStudents(any());
    }

    @Test
    @DisplayName("simulatePayment: Ném lỗi PAYMENT_SIMULATION_DISABLED khi tính năng bị tắt bởi cấu hình")
    void simulatePayment_SimulationDisabled_ThrowsAppException() {
        ReflectionTestUtils.setField(paymentService, "simulationEnabled", false);

        assertThatThrownBy(() -> paymentService.simulatePayment(1000L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_SIMULATION_DISABLED));

        verifyNoInteractions(orderRepository, paymentRepository, enrollmentRepository, courseRepository);
    }

    @Test
    @DisplayName("simulatePayment: Ném lỗi ORDER_ACCESS_DENIED khi học viên thanh toán order của học viên khác")
    void simulatePayment_CrossUserAccess_ThrowsAppException() {
        pendingOrder.setUser(otherStudent);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(orderRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> paymentService.simulatePayment(1000L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ORDER_ACCESS_DENIED));

        verify(paymentRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
        verify(courseRepository, never()).incrementTotalStudents(any());
    }

    @Test
    @DisplayName("simulatePayment: Ném lỗi ORDER_INVALID_STATUS khi order ở trạng thái CANCELLED")
    void simulatePayment_InvalidStatus_Cancelled_ThrowsAppException() {
        pendingOrder.setStatus(OrderStatus.CANCELLED);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(orderRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> paymentService.simulatePayment(1000L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ORDER_INVALID_STATUS));

        verify(paymentRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
        verify(courseRepository, never()).incrementTotalStudents(any());
    }

    @Test
    @DisplayName("simulatePayment: Ném lỗi ORDER_NOT_FOUND khi order không tồn tại")
    void simulatePayment_OrderNotFound_ThrowsAppException() {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(orderRepository.findByIdForUpdate(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.simulatePayment(9999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("simulatePayment: Không tăng counter nếu học viên đã có ACTIVE enrollment")
    void simulatePayment_AlreadyActiveEnrollment_DoesNotIncrementCounter() {
        CourseEnrollment activeEnrollment = CourseEnrollment.builder()
                .id(205L)
                .user(student)
                .course(paidCourse)
                .status(EnrollmentStatus.ACTIVE)
                .progressPercent(10)
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(orderRepository.findByIdForUpdate(1000L)).thenReturn(Optional.of(pendingOrder));
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 10L)).thenReturn(Optional.of(activeEnrollment));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(503L);
            return p;
        });

        PaymentRes result = paymentService.simulatePayment(1000L);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(courseRepository, never()).incrementTotalStudents(any());
    }
}
