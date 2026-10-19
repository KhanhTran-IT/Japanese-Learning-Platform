package com.japaneselearning.module_payment.repository;

import com.japaneselearning.module_payment.entity.Payment;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionCode(String transactionCode);

    List<Payment> findByOrderId(Long orderId);

    Optional<Payment> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(Long orderId, PaymentStatus status);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
