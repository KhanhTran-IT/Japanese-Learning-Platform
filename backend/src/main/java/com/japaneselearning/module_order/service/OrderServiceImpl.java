package com.japaneselearning.module_order.service;

import com.japaneselearning.common.audit.AuditAction;
import com.japaneselearning.common.audit.AuditOutcome;
import com.japaneselearning.common.audit.AuditService;
import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.policy.EnrollmentAccessPolicy;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public OrderRes createOrder(OrderCreateReq req) {
        User currentUser = getCurrentUser();

        // Lock user row pessimistically to serialize concurrent order creation attempts for the same user
        userRepository.findByIdForUpdate(currentUser.getId());

        String idempotencyKey = (req.getIdempotencyKey() != null && !req.getIdempotencyKey().isBlank())
                ? req.getIdempotencyKey().trim()
                : null;

        // Idempotency check: if key already exists for this authenticated user, return existing order or detect key reuse conflict
        if (idempotencyKey != null) {
            Optional<Order> existingOrderOpt = orderRepository.findByUserIdAndIdempotencyKey(currentUser.getId(), idempotencyKey);
            if (existingOrderOpt.isPresent()) {
                Order existingOrder = existingOrderOpt.get();
                boolean sameCourse = existingOrder.getItems() != null && existingOrder.getItems().stream()
                        .anyMatch(item -> item.getCourse() != null && item.getCourse().getId().equals(req.getCourseId()));
                if (!sameCourse) {
                    throw new AppException(ErrorCode.ORDER_IDEMPOTENCY_KEY_CONFLICT);
                }
                return mapToOrderRes(existingOrder);
            }
        }

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
                EnrollmentAccessPolicy.ACCESSIBLE_STATUSES
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
                .idempotencyKey(idempotencyKey)
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

        Order savedOrder;
        try {
            savedOrder = orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException e) {
            if (idempotencyKey != null) {
                Optional<Order> raceOrder = orderRepository.findByUserIdAndIdempotencyKey(currentUser.getId(), idempotencyKey);
                if (raceOrder.isPresent()) {
                    Order existing = raceOrder.get();
                    boolean sameCourse = existing.getItems() != null && existing.getItems().stream()
                            .anyMatch(i -> i.getCourse() != null && i.getCourse().getId().equals(req.getCourseId()));
                    if (sameCourse) {
                        return mapToOrderRes(existing);
                    }
                    throw new AppException(ErrorCode.ORDER_IDEMPOTENCY_KEY_CONFLICT);
                }
            }
            if (orderRepository.existsByUserIdAndCourseIdAndStatus(currentUser.getId(), course.getId(), OrderStatus.PENDING)) {
                throw new AppException(ErrorCode.ORDER_DUPLICATE_PENDING);
            }
            throw e;
        }

        auditService.recordAsync(AuditAction.ORDER_CREATE, "ORDER", String.valueOf(savedOrder.getId()), AuditOutcome.SUCCESS,
                Map.of("orderCode", savedOrder.getOrderCode(), "courseId", course.getId(), "finalAmount", savedOrder.getFinalAmount()));

        return mapToOrderRes(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderRes> getMyOrders(Pageable pageable) {
        User currentUser = getCurrentUser();

        // Enforce deterministic sorting: createdAt DESC, id DESC
        Pageable deterministicPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        Page<Order> orderPage = orderRepository.findByUserId(currentUser.getId(), deterministicPageable);

        return PageResponse.<OrderRes>builder()
                .currentPage(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .data(orderPage.getContent().stream()
                        .map(this::mapToOrderRes)
                        .toList())
                .build();
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
                .idempotencyKey(order.getIdempotencyKey())
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
