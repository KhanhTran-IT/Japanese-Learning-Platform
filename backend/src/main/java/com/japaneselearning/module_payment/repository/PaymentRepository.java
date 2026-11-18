package com.japaneselearning.module_payment.repository;

import com.japaneselearning.module_payment.entity.Payment;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionCode(String transactionCode);

    List<Payment> findByOrderId(Long orderId);

    Optional<Payment> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(Long orderId, PaymentStatus status);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);

    /**
     * Purges raw response payloads older than the retention threshold cutoff,
     * replacing them with a policy marker to comply with data minimization requirements
     * while retaining statutory financial transaction records.
     *
     * @param cutoff Date before which raw payloads should be purged
     * @param replacement Marker string (e.g. [PURGED_BY_RETENTION_POLICY])
     * @return Number of records updated
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Payment p SET p.rawResponse = :replacement WHERE p.createdAt < :cutoff AND p.rawResponse IS NOT NULL AND p.rawResponse != :replacement")
    int purgeExpiredRawResponses(@Param("cutoff") LocalDateTime cutoff, @Param("replacement") String replacement);
}
