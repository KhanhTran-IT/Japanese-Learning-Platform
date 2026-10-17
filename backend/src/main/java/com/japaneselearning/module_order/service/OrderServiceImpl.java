package com.japaneselearning.module_order.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.dto.OrderItemRes;
import com.japaneselearning.module_order.dto.OrderRes;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.entity.OrderItem;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderRes createOrder(OrderCreateReq req) {
        User currentUser = getCurrentUser();

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_AVAILABLE_FOR_ENROLLMENT);
        }

        if (course.getCourseType() != CourseType.PAID) {
            throw new AppException(ErrorCode.ORDER_COURSE_NOT_PAID);
        }

        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                currentUser.getId(),
                course.getId(),
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED)
        );
        if (alreadyEnrolled) {
            throw new AppException(ErrorCode.USER_ALREADY_ENROLLED);
        }

        boolean hasPendingOrder = orderRepository.existsByUserIdAndCourseIdAndStatus(
                currentUser.getId(),
                course.getId(),
                OrderStatus.PENDING
        );
        if (hasPendingOrder) {
            throw new AppException(ErrorCode.ORDER_DUPLICATE_PENDING);
        }

        BigDecimal effectivePrice = determineEffectivePrice(course);

        String orderCode = generateUniqueOrderCode();

        Order order = Order.builder()
                .user(currentUser)
                .orderCode(orderCode)
                .totalAmount(effectivePrice)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(effectivePrice)
                .status(OrderStatus.PENDING)
                .note(req.getNote())
                .build();

        OrderItem item = OrderItem.builder()
                .course(course)
                .courseTitle(course.getTitle())
                .price(effectivePrice)
                .quantity(1)
                .build();

        order.addItem(item);

        Order savedOrder = orderRepository.save(order);
        return mapToOrderRes(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderRes> getMyOrders() {
        User currentUser = getCurrentUser();
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return orders.stream().map(this::mapToOrderRes).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderRes getOrderById(Long id) {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return mapToOrderRes(order);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private BigDecimal determineEffectivePrice(Course course) {
        if (course.getSalePrice() != null && course.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
            return course.getSalePrice();
        }
        return course.getOriginalPrice() != null ? course.getOriginalPrice() : BigDecimal.ZERO;
    }

    private String generateUniqueOrderCode() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String code;
        do {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
            code = "ORD-" + timestamp + "-" + suffix;
        } while (orderRepository.existsByOrderCode(code));
        return code;
    }

    private OrderRes mapToOrderRes(Order order) {
        List<OrderItemRes> itemResponses = order.getItems() != null
                ? order.getItems().stream().map(this::mapToOrderItemRes).toList()
                : List.of();

        return OrderRes.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus())
                .couponCode(order.getCouponCode())
                .note(order.getNote())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemRes mapToOrderItemRes(OrderItem item) {
        return OrderItemRes.builder()
                .id(item.getId())
                .courseId(item.getCourse() != null ? item.getCourse().getId() : null)
                .courseTitle(item.getCourseTitle())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
