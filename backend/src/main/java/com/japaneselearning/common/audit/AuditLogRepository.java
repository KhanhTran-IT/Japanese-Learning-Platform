package com.japaneselearning.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Append-only repository for {@link AuditLog}.
 * <p>
 * Enforces the append-only design by rejecting any delete operations with
 * {@link UnsupportedOperationException}.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByActorEmail(String actorEmail, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByTargetTypeAndTargetId(String targetType, String targetId, Pageable pageable);

    List<AuditLog> findByCorrelationId(String correlationId);

    Page<AuditLog> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    // --- Append-Only Guarantees: Explicitly disable delete operations ---

    @Override
    default void delete(AuditLog entity) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted");
    }

    @Override
    default void deleteById(Long id) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted");
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted");
    }

    @Override
    default void deleteAll(Iterable<? extends AuditLog> entities) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted");
    }

    @Override
    default void deleteAllById(Iterable<? extends Long> ids) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted");
    }
}
