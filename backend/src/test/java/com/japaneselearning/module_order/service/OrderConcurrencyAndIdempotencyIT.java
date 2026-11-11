package com.japaneselearning.module_order.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.dto.OrderRes;
import com.japaneselearning.module_order.entity.Order;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class OrderConcurrencyAndIdempotencyIT {

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
    private User teacher;
    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        studentA = userRepository.saveAndFlush(User.builder()
                .email("student_a@example.com")
                .fullName("Student A")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .build());

        studentB = userRepository.saveAndFlush(User.builder()
                .email("student_b@example.com")
                .fullName("Student B")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .build());

        teacher = userRepository.saveAndFlush(User.builder()
                .email("teacher@example.com")
                .fullName("Teacher Master")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .build());

        course1 = courseRepository.saveAndFlush(Course.builder()
                .teacher(teacher)
                .title("Khóa học N3 Cấp Tốc")
                .slug("khoa-hoc-n3-cap-toc-concurrency")
                .level(CourseLevel.N3)
                .courseType(CourseType.PAID)
                .originalPrice(new BigDecimal("500000.00"))
                .salePrice(new BigDecimal("350000.00"))
                .status(CourseStatus.PUBLISHED)
                .totalStudents(0)
                .build());

        course2 = courseRepository.saveAndFlush(Course.builder()
                .teacher(teacher)
                .title("Khóa học N2 Chuyên Sâu")
                .slug("khoa-hoc-n2-chuyen-sau-concurrency")
                .level(CourseLevel.N2)
                .courseType(CourseType.PAID)
                .originalPrice(new BigDecimal("800000.00"))
                .salePrice(new BigDecimal("600000.00"))
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
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList()));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Idempotent retry: gửi lại cùng idempotencyKey và cùng courseId trả về đúng đơn hàng cũ mà không tạo mới")
    void testIdempotentRetry_ReturnsExistingOrder() {
        authenticateAs(studentA);

        OrderCreateReq req1 = OrderCreateReq.builder()
                .courseId(course1.getId())
                .idempotencyKey("idem-retry-001")
                .note("Lần gọi 1")
                .build();

        OrderRes res1 = orderService.createOrder(req1);
        assertThat(res1).isNotNull();
        assertThat(res1.getIdempotencyKey()).isEqualTo("idem-retry-001");
        assertThat(res1.getStatus()).isEqualTo(OrderStatus.PENDING);

        // Retry với cùng key và cùng courseId
        OrderCreateReq req2 = OrderCreateReq.builder()
                .courseId(course1.getId())
                .idempotencyKey("idem-retry-001")
                .note("Lần gọi 2 (retry)")
                .build();

        OrderRes res2 = orderService.createOrder(req2);
        assertThat(res2.getId()).isEqualTo(res1.getId());
        assertThat(res2.getOrderCode()).isEqualTo(res1.getOrderCode());
        assertThat(res2.getFinalAmount()).isEqualByComparingTo(res1.getFinalAmount());

        // Kiểm tra database chỉ có duy nhất 1 đơn hàng
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(studentA.getId());
        assertThat(orders).hasSize(1);
    }

    @Test
    @DisplayName("Key reuse conflict: dùng lại cùng idempotencyKey cho khóa học khác bị từ chối 409 ORDER_IDEMPOTENCY_KEY_CONFLICT")
    void testKeyReuse_DifferentCourse_ThrowsConflict() {
        authenticateAs(studentA);

        OrderCreateReq req1 = OrderCreateReq.builder()
                .courseId(course1.getId())
                .idempotencyKey("shared-key-reuse")
                .build();

        orderService.createOrder(req1);

        // Dùng lại cùng key nhưng course2
        OrderCreateReq req2 = OrderCreateReq.builder()
                .courseId(course2.getId())
                .idempotencyKey("shared-key-reuse")
                .build();

        assertThatThrownBy(() -> orderService.createOrder(req2))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_IDEMPOTENCY_KEY_CONFLICT);

        // Database chỉ có đơn hàng của course1
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(studentA.getId());
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getItems().get(0).getCourse().getId()).isEqualTo(course1.getId());
    }

    @Test
    @DisplayName("Idempotency key scoped to authenticated user: 2 học viên dùng cùng key không xung đột nhau")
    void testIdempotencyKeyScopedToUser_Isolation() {
        // Student A tạo đơn với key 'global-key-100'
        authenticateAs(studentA);
        OrderRes resA = orderService.createOrder(OrderCreateReq.builder()
                .courseId(course1.getId())
                .idempotencyKey("global-key-100")
                .build());

        // Student B tạo đơn với cùng key 'global-key-100'
        authenticateAs(studentB);
        OrderRes resB = orderService.createOrder(OrderCreateReq.builder()
                .courseId(course1.getId())
                .idempotencyKey("global-key-100")
                .build());

        assertThat(resA.getId()).isNotEqualTo(resB.getId());
        assertThat(resA.getOrderCode()).isNotEqualTo(resB.getOrderCode());
        assertThat(resA.getUserId()).isEqualTo(studentA.getId());
        assertThat(resB.getUserId()).isEqualTo(studentB.getId());

        // Cả 2 đơn hàng đều tồn tại độc lập
        assertThat(orderRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Preserve server-owned price snapshot: lưu đúng giá snapshot dù sau đó giá khóa học thay đổi")
    void testServerOwnedPriceSnapshot_Preserved() {
        authenticateAs(studentA);

        OrderRes res = orderService.createOrder(OrderCreateReq.builder()
                .courseId(course1.getId())
                .build());

        assertThat(res.getFinalAmount()).isEqualByComparingTo(new BigDecimal("350000.00"));
        assertThat(res.getTotalAmount()).isEqualByComparingTo(new BigDecimal("350000.00"));

        // Giảng viên đổi giá khóa học tăng lên 450,000
        course1.setSalePrice(new BigDecimal("450000.00"));
        courseRepository.saveAndFlush(course1);

        // Xem lại đơn hàng đã tạo, giá snapshot vẫn là 350,000
        OrderRes fetchedOrder = orderService.getOrderById(res.getId());
        assertThat(fetchedOrder.getFinalAmount()).isEqualByComparingTo(new BigDecimal("350000.00"));
        assertThat(fetchedOrder.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("350000.00"));
    }

    @Test
    @DisplayName("Concurrency: 10 luồng đồng thời tạo đơn cho cùng 1 khóa học với keys khác nhau -> chỉ đúng 1 thành công, 9 thất bại ORDER_DUPLICATE_PENDING")
    void testConcurrentSubmissions_OnePendingOrderRuleEnforced() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicatePendingCount = new AtomicInteger(0);
        AtomicInteger otherErrors = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(new UsernamePasswordAuthenticationToken(studentA.getEmail(), null, Collections.emptyList()));
                    SecurityContextHolder.setContext(context);

                    startLatch.await();

                    OrderCreateReq req = OrderCreateReq.builder()
                            .courseId(course1.getId())
                            .idempotencyKey("diff-key-" + index)
                            .build();

                    orderService.createOrder(req);
                    successCount.incrementAndGet();
                } catch (AppException e) {
                    if (e.getErrorCode() == ErrorCode.ORDER_DUPLICATE_PENDING) {
                        duplicatePendingCount.incrementAndGet();
                    } else {
                        otherErrors.incrementAndGet();
                    }
                } catch (Exception e) {
                    otherErrors.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicatePendingCount.get()).isEqualTo(numberOfThreads - 1);
        assertThat(otherErrors.get()).isEqualTo(0);

        // Database chỉ có chính xác 1 đơn hàng PENDING
        List<Order> ordersInDb = orderRepository.findByUserIdOrderByCreatedAtDesc(studentA.getId());
        assertThat(ordersInDb).hasSize(1);
        assertThat(ordersInDb.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Concurrency: 10 luồng đồng thời tạo đơn với CÙNG 1 idempotencyKey -> tất cả 10 thành công trả về cùng đơn hàng duy nhất")
    void testConcurrentSubmissions_SameIdempotencyKey_AllSucceedIdempotently() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        Set<Long> orderIds = ConcurrentHashMap.newKeySet();
        AtomicInteger errorCount = new AtomicInteger(0);

        String sharedKey = "concurrent-same-key-999";

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(new UsernamePasswordAuthenticationToken(studentA.getEmail(), null, Collections.emptyList()));
                    SecurityContextHolder.setContext(context);

                    startLatch.await();

                    OrderCreateReq req = OrderCreateReq.builder()
                            .courseId(course1.getId())
                            .idempotencyKey(sharedKey)
                            .build();

                    OrderRes res = orderService.createOrder(req);
                    orderIds.add(res.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        assertThat(errorCount.get()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(numberOfThreads);
        assertThat(orderIds).hasSize(1); // Tất cả các luồng đều nhận cùng 1 orderId

        // Database chỉ có 1 order duy nhất
        List<Order> ordersInDb = orderRepository.findByUserIdOrderByCreatedAtDesc(studentA.getId());
        assertThat(ordersInDb).hasSize(1);
    }
}
