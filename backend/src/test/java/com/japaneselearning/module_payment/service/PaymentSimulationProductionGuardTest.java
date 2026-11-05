package com.japaneselearning.module_payment.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.observability.ObservabilityMetrics;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_payment.dto.PaymentCreateReq;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.gateway.PaymentGatewayRegistry;
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
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Simulation Production Guard Unit Tests")
class PaymentSimulationProductionGuardTest {

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

    @Mock
    private PaymentGatewayRegistry gatewayRegistry;

    @Mock
    private Environment environment;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User student;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .email("student@example.com")
                .fullName("Student")
                .build();

        pendingOrder = Order.builder()
                .id(100L)
                .orderCode("ORD-100")
                .user(student)
                .finalAmount(BigDecimal.valueOf(500000))
                .status(OrderStatus.PENDING)
                .build();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(student.getEmail(), "password"));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should throw PAYMENT_SIMULATION_DISABLED when simulationEnabled is false")
    void simulatePayment_Disabled_ThrowsException() {
        ReflectionTestUtils.setField(paymentService, "simulationEnabled", false);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        assertThatThrownBy(() -> paymentService.simulatePayment(100L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_SIMULATION_DISABLED));

        verify(orderRepository, never()).findByIdForUpdate(any());
    }

    @Test
    @DisplayName("Should strictly reject simulation when active profile is 'prod' even if simulationEnabled is true")
    void simulatePayment_InProdProfile_StrictlyDisabled() {
        ReflectionTestUtils.setField(paymentService, "simulationEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        assertThatThrownBy(() -> paymentService.simulatePayment(100L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_SIMULATION_DISABLED));

        verify(orderRepository, never()).findByIdForUpdate(any());
    }

    @Test
    @DisplayName("Should reject createPayment with SIMULATION method when running in 'prod' profile")
    void createPayment_SimulationMethod_InProdProfile_ThrowsException() {
        ReflectionTestUtils.setField(paymentService, "simulationEnabled", true);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(pendingOrder));

        PaymentCreateReq req = PaymentCreateReq.builder()
                .orderId(100L)
                .paymentMethod(PaymentMethod.SIMULATION)
                .build();

        assertThatThrownBy(() -> paymentService.createPayment(req))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PAYMENT_SIMULATION_DISABLED));

        verify(paymentRepository, never()).save(any());
    }
}
