package com.japaneselearning.module_order.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.dto.OrderRes;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User student;
    private Course paidCourse;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(100L)
                .email("student@example.com")
                .fullName("Test Student")
                .build();

        paidCourse = Course.builder()
                .id(1L)
                .title("Khóa học N3 Cấp Tốc")
                .status(CourseStatus.PUBLISHED)
                .courseType(CourseType.PAID)
                .originalPrice(new BigDecimal("500000.00"))
                .salePrice(new BigDecimal("350000.00"))
                .build();

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(student.getEmail(), "password")
        );
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Tạo order thành công khi có salePrice: snapshot đúng salePrice và title")
    void createOrder_Success_WithSalePrice() {
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).note("Ghi chú đơn hàng").build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(paidCourse));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(eq(100L), eq(1L), any())).thenReturn(false);
        when(orderRepository.existsByUserIdAndCourseIdAndStatus(100L, 1L, OrderStatus.PENDING)).thenReturn(false);
        when(orderRepository.existsByOrderCode(any())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(10L);
            return o;
        });

        OrderRes res = orderService.createOrder(req);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(res.getOrderCode()).startsWith("ORD-");
        assertThat(res.getTotalAmount()).isEqualByComparingTo(new BigDecimal("350000.00"));
        assertThat(res.getFinalAmount()).isEqualByComparingTo(new BigDecimal("350000.00"));
        assertThat(res.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(res.getNote()).isEqualTo("Ghi chú đơn hàng");
        assertThat(res.getItems()).hasSize(1);
        assertThat(res.getItems().get(0).getCourseTitle()).isEqualTo("Khóa học N3 Cấp Tốc");
        assertThat(res.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("350000.00"));
        assertThat(res.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tạo order thành công khi không có salePrice: snapshot originalPrice")
    void createOrder_Success_WithOriginalPrice() {
        paidCourse.setSalePrice(null);
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(paidCourse));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(eq(100L), eq(1L), any())).thenReturn(false);
        when(orderRepository.existsByUserIdAndCourseIdAndStatus(100L, 1L, OrderStatus.PENDING)).thenReturn(false);
        when(orderRepository.existsByOrderCode(any())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(11L);
            return o;
        });

        OrderRes res = orderService.createOrder(req);

        assertThat(res).isNotNull();
        assertThat(res.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500000.00"));
        assertThat(res.getFinalAmount()).isEqualByComparingTo(new BigDecimal("500000.00"));
        assertThat(res.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("500000.00"));
    }

    @Test
    @DisplayName("Tạo order thất bại khi khóa học không tồn tại")
    void createOrder_CourseNotFound_ThrowsAppException() {
        OrderCreateReq req = OrderCreateReq.builder().courseId(999L).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("Tạo order thất bại khi khóa học ở trạng thái DRAFT")
    void createOrder_CourseDraft_ThrowsCourseNotAvailable() {
        paidCourse.setStatus(CourseStatus.DRAFT);
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(paidCourse));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_AVAILABLE_FOR_ENROLLMENT);
    }

    @Test
    @DisplayName("Tạo order thất bại khi khóa học ở trạng thái HIDDEN")
    void createOrder_CourseHidden_ThrowsCourseNotAvailable() {
        paidCourse.setStatus(CourseStatus.HIDDEN);
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(paidCourse));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_AVAILABLE_FOR_ENROLLMENT);
    }

    @Test
    @DisplayName("Tạo order thất bại khi khóa học là FREE")
    void createOrder_FreeCourse_ThrowsOrderCourseNotPaid() {
        paidCourse.setCourseType(CourseType.FREE);
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(paidCourse));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_COURSE_NOT_PAID);
    }

    @Test
    @DisplayName("Tạo order thất bại khi user đã enroll ACTIVE")
    void createOrder_UserAlreadyEnrolledActive_ThrowsUserAlreadyEnrolled() {
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(paidCourse));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                eq(100L), eq(1L), eq(List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED))))
                .thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_ENROLLED);
    }

    @Test
    @DisplayName("Tạo order thất bại khi user đã enroll COMPLETED")
    void createOrder_UserAlreadyEnrolledCompleted_ThrowsUserAlreadyEnrolled() {
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(paidCourse));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                eq(100L), eq(1L), eq(List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED))))
                .thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_ENROLLED);
    }

    @Test
    @DisplayName("Policy chống duplicate pending order: chặn tạo order mới khi đã có pending order cho cùng khóa học")
    void createOrder_DuplicatePendingOrder_ThrowsOrderDuplicatePending() {
        OrderCreateReq req = OrderCreateReq.builder().courseId(1L).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(paidCourse));
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(eq(100L), eq(1L), any())).thenReturn(false);
        when(orderRepository.existsByUserIdAndCourseIdAndStatus(100L, 1L, OrderStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_DUPLICATE_PENDING);
    }

    @Test
    @DisplayName("getMyOrders trả về PageResponse với deterministic sort và dữ liệu của student hiện tại")
    void getMyOrders_Success() {
        Order order1 = Order.builder().id(1L).user(student).orderCode("ORD-1").status(OrderStatus.PENDING).build();
        Order order2 = Order.builder().id(2L).user(student).orderCode("ORD-2").status(OrderStatus.PAID).build();
        Page<Order> orderPage = new PageImpl<>(List.of(order2, order1), PageRequest.of(0, 10), 2);

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findByUserId(eq(100L), any(Pageable.class))).thenReturn(orderPage);

        PageResponse<OrderRes> results = orderService.getMyOrders(PageRequest.of(0, 10));

        assertThat(results).isNotNull();
        assertThat(results.getCurrentPage()).isEqualTo(0);
        assertThat(results.getPageSize()).isEqualTo(10);
        assertThat(results.getTotalPages()).isEqualTo(1);
        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getData()).hasSize(2);
        assertThat(results.getData().get(0).getOrderCode()).isEqualTo("ORD-2");
        assertThat(results.getData().get(1).getOrderCode()).isEqualTo("ORD-1");
    }

    @Test
    @DisplayName("getOrderById thành công khi là chủ sở hữu đơn hàng")
    void getOrderById_Owner_Success() {
        Order order = Order.builder().id(1L).user(student).orderCode("ORD-1").status(OrderStatus.PENDING).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findWithItemsById(1L)).thenReturn(Optional.of(order));

        OrderRes res = orderService.getOrderById(1L);

        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getOrderCode()).isEqualTo("ORD-1");
    }

    @Test
    @DisplayName("getOrderById thất bại khi đơn hàng không tồn tại")
    void getOrderById_NotFound_ThrowsOrderNotFound() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findWithItemsById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("getOrderById thất bại khi học viên khác cố tình xem đơn hàng (cross-user access forbidden)")
    void getOrderById_CrossUser_ThrowsOrderAccessDenied() {
        User otherUser = User.builder().id(200L).email("other@example.com").build();
        Order otherOrder = Order.builder().id(1L).user(otherUser).orderCode("ORD-1").status(OrderStatus.PENDING).build();

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findWithItemsById(1L)).thenReturn(Optional.of(otherOrder));

        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ACCESS_DENIED);
    }
}
