package com.japaneselearning.module_order.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_order.dto.OrderRes;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.entity.OrderItem;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderPaginationAndIsolationIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private User studentA;
    private User studentB;
    private Course course;

    @BeforeEach
    void setUp() {
        studentA = userRepository.findByEmail("student_a@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_a@example.com")
                        .fullName("Student A")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        studentB = userRepository.findByEmail("student_b@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_b@example.com")
                        .fullName("Student B")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        User teacher = userRepository.findByEmail("teacher_pagination@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("teacher_pagination@example.com")
                        .fullName("Teacher Pagination")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        course = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N3 Cấp Tốc")
                .slug("khoa-hoc-n3-cap-toc-pagination-it")
                .level(CourseLevel.N3)
                .courseType(CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(500000))
                .salePrice(BigDecimal.valueOf(350000))
                .status(CourseStatus.PUBLISHED)
                .totalStudents(0)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), "password"));
        SecurityContextHolder.setContext(context);
    }

    private Order createOrder(User user, String orderCode, OrderStatus status, LocalDateTime createdAt) {
        Order order = Order.builder()
                .user(user)
                .orderCode(orderCode)
                .totalAmount(BigDecimal.valueOf(350000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(350000))
                .status(status)
                .createdAt(createdAt)
                .build();

        OrderItem item = OrderItem.builder()
                .course(course)
                .courseTitle(course.getTitle())
                .price(BigDecimal.valueOf(350000))
                .quantity(1)
                .build();
        order.addItem(item);

        return orderRepository.save(order);
    }

    @Test
    @DisplayName("Pagination: Kiểm tra phân trang nhiều trang với metadata chính xác")
    void testOrderPagination_MultiplePagesAndMetadata() {
        authenticateAs(studentA);

        LocalDateTime baseTime = LocalDateTime.of(2026, 10, 1, 10, 0, 0);
        for (int i = 1; i <= 5; i++) {
            createOrder(studentA, "ORD-A-" + i, OrderStatus.PENDING, baseTime.plusHours(i));
        }

        // Page 0, size 2
        PageResponse<OrderRes> page0 = orderService.getMyOrders(PageRequest.of(0, 2));
        assertThat(page0.getCurrentPage()).isEqualTo(0);
        assertThat(page0.getPageSize()).isEqualTo(2);
        assertThat(page0.getTotalPages()).isEqualTo(3);
        assertThat(page0.getTotalElements()).isEqualTo(5);
        assertThat(page0.getData()).hasSize(2);
        assertThat(page0.getData().get(0).getOrderCode()).isEqualTo("ORD-A-5");
        assertThat(page0.getData().get(1).getOrderCode()).isEqualTo("ORD-A-4");

        // Page 1, size 2
        PageResponse<OrderRes> page1 = orderService.getMyOrders(PageRequest.of(1, 2));
        assertThat(page1.getCurrentPage()).isEqualTo(1);
        assertThat(page1.getPageSize()).isEqualTo(2);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getData()).hasSize(2);
        assertThat(page1.getData().get(0).getOrderCode()).isEqualTo("ORD-A-3");
        assertThat(page1.getData().get(1).getOrderCode()).isEqualTo("ORD-A-2");

        // Page 2, size 2
        PageResponse<OrderRes> page2 = orderService.getMyOrders(PageRequest.of(2, 2));
        assertThat(page2.getCurrentPage()).isEqualTo(2);
        assertThat(page2.getPageSize()).isEqualTo(2);
        assertThat(page2.getTotalPages()).isEqualTo(3);
        assertThat(page2.getTotalElements()).isEqualTo(5);
        assertThat(page2.getData()).hasSize(1);
        assertThat(page2.getData().get(0).getOrderCode()).isEqualTo("ORD-A-1");

        // Page 3, size 2 (out of bounds page)
        PageResponse<OrderRes> page3 = orderService.getMyOrders(PageRequest.of(3, 2));
        assertThat(page3.getCurrentPage()).isEqualTo(3);
        assertThat(page3.getData()).isEmpty();

        // Verify no duplicate IDs across all pages
        Set<Long> collectedIds = new HashSet<>();
        page0.getData().forEach(o -> collectedIds.add(o.getId()));
        page1.getData().forEach(o -> collectedIds.add(o.getId()));
        page2.getData().forEach(o -> collectedIds.add(o.getId()));
        assertThat(collectedIds).hasSize(5);
    }

    @Test
    @DisplayName("Deterministic sorting: Tie-breaker id DESC khi các đơn hàng có cùng createdAt")
    void testDeterministicTieBreaker_WhenTimestampsAreEqual() {
        authenticateAs(studentA);

        LocalDateTime sameTime = LocalDateTime.of(2026, 10, 15, 12, 0, 0);
        Order o1 = createOrder(studentA, "ORD-SAME-1", OrderStatus.PENDING, sameTime);
        Order o2 = createOrder(studentA, "ORD-SAME-2", OrderStatus.PENDING, sameTime);
        Order o3 = createOrder(studentA, "ORD-SAME-3", OrderStatus.PENDING, sameTime);

        // Deterministic sort: id DESC
        PageResponse<OrderRes> page0 = orderService.getMyOrders(PageRequest.of(0, 2));
        PageResponse<OrderRes> page1 = orderService.getMyOrders(PageRequest.of(1, 2));

        assertThat(page0.getData()).hasSize(2);
        assertThat(page0.getData().get(0).getId()).isEqualTo(o3.getId());
        assertThat(page0.getData().get(1).getId()).isEqualTo(o2.getId());

        assertThat(page1.getData()).hasSize(1);
        assertThat(page1.getData().get(0).getId()).isEqualTo(o1.getId());
    }

    @Test
    @DisplayName("Cross-user isolation: Học viên chỉ thấy đơn hàng của chính mình trong danh sách phân trang")
    void testCrossUserIsolation_StudentOnlySeesTheirOwnOrders() {
        LocalDateTime now = LocalDateTime.now();
        Order orderA1 = createOrder(studentA, "ORD-ISO-A1", OrderStatus.PENDING, now.minusHours(3));
        Order orderA2 = createOrder(studentA, "ORD-ISO-A2", OrderStatus.PAID, now.minusHours(2));
        Order orderA3 = createOrder(studentA, "ORD-ISO-A3", OrderStatus.PENDING, now.minusHours(1));

        Order orderB1 = createOrder(studentB, "ORD-ISO-B1", OrderStatus.PENDING, now.minusHours(2));
        Order orderB2 = createOrder(studentB, "ORD-ISO-B2", OrderStatus.PAID, now.minusHours(1));

        // 1. Authenticate as Student A
        authenticateAs(studentA);
        PageResponse<OrderRes> studentAPage = orderService.getMyOrders(PageRequest.of(0, 10));

        assertThat(studentAPage.getTotalElements()).isEqualTo(3);
        List<String> studentAOrderCodes = studentAPage.getData().stream().map(OrderRes::getOrderCode).toList();
        assertThat(studentAOrderCodes).containsExactly("ORD-ISO-A3", "ORD-ISO-A2", "ORD-ISO-A1");
        assertThat(studentAOrderCodes).doesNotContain("ORD-ISO-B1", "ORD-ISO-B2");

        // Student A can access their own order by ID
        assertThat(orderService.getOrderById(orderA1.getId())).isNotNull();

        // Student A cannot access Student B's order by ID
        assertThatThrownBy(() -> orderService.getOrderById(orderB1.getId()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ACCESS_DENIED);

        // 2. Authenticate as Student B
        authenticateAs(studentB);
        PageResponse<OrderRes> studentBPage = orderService.getMyOrders(PageRequest.of(0, 10));

        assertThat(studentBPage.getTotalElements()).isEqualTo(2);
        List<String> studentBOrderCodes = studentBPage.getData().stream().map(OrderRes::getOrderCode).toList();
        assertThat(studentBOrderCodes).containsExactly("ORD-ISO-B2", "ORD-ISO-B1");
        assertThat(studentBOrderCodes).doesNotContain("ORD-ISO-A1", "ORD-ISO-A2", "ORD-ISO-A3");

        // Student B can access their own order by ID
        assertThat(orderService.getOrderById(orderB1.getId())).isNotNull();

        // Student B cannot access Student A's order by ID
        assertThatThrownBy(() -> orderService.getOrderById(orderA1.getId()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ACCESS_DENIED);
    }
}
