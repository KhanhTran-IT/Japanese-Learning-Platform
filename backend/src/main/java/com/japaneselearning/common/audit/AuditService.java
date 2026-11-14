package com.japaneselearning.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Service interface for recording and querying the append-only audit trail.
 */
public interface AuditService {

    /**
     * Record an audit event synchronously in an isolated transaction (REQUIRES_NEW).
     * Recommended for financial, administrative, and critical security actions.
     *
     * @param event The audit event data
     */
    void record(AuditEvent event);

    /**
     * Record an audit event asynchronously in a background thread with REQUIRES_NEW.
     * Recommended for high-frequency, non-blocking authentication events.
     *
     * @param event The audit event data
     */
    void recordAsync(AuditEvent event);

    /**
     * Helper to synchronously record an audit event with metadata map.
     */
    void record(AuditAction action, String targetType, String targetId, AuditOutcome outcome, Map<String, Object> metadata);

    /**
     * Helper to synchronously record an audit event with an error message and metadata map.
     */
    void record(AuditAction action, String targetType, String targetId, AuditOutcome outcome, String errorMessage, Map<String, Object> metadata);

    /**
     * Helper to asynchronously record an audit event with metadata map.
     */
    void recordAsync(AuditAction action, String targetType, String targetId, AuditOutcome outcome, Map<String, Object> metadata);

    /**
     * Helper to asynchronously record an audit event with an error message and metadata map.
     */
    void recordAsync(AuditAction action, String targetType, String targetId, AuditOutcome outcome, String errorMessage, Map<String, Object> metadata);

    /**
     * Query audit logs with filter criteria and pagination (Admin-only).
     *
     * @param filter Filter criteria
     * @param pageable Pagination and sort
     * @return Page of audit log records
     */
    Page<AuditLogRes> queryLogs(AuditLogFilterReq filter, Pageable pageable);
}
