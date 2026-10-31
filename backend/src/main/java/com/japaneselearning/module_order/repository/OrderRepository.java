package com.japaneselearning.module_order.repository;

import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.course"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.course"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findWithItemsById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"items", "items.course", "user"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o " +
           "JOIN o.items item " +
           "WHERE o.user.id = :userId " +
           "AND item.course.id = :courseId " +
           "AND o.status = :status")
    boolean existsByUserIdAndCourseIdAndStatus(@Param("userId") Long userId,
                                              @Param("courseId") Long courseId,
                                              @Param("status") OrderStatus status);
}
