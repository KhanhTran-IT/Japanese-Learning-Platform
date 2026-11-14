package com.japaneselearning.common.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Event model representing an audit payload before persistence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    /** Actor ID (e.g. user ID). If null, can be auto-resolved from SecurityContext. */
    private Long actorId;

    /** Actor email/username. If null, auto-resolved from SecurityContext. */
    private String actorEmail;

    /** Actor roles (e.g. "ROLE_ADMIN"). If null, auto-resolved from SecurityContext. */
    private String actorRole;

    /** The audited action. */
    private AuditAction action;

    /** Target entity type (e.g. "USER", "COURSE", "ORDER", "PAYMENT"). */
    private String targetType;

    /** Target entity identifier (e.g. ID, order code). */
    private String targetId;

    /** Outcome of the action (SUCCESS or FAILURE). */
    @Builder.Default
    private AuditOutcome outcome = AuditOutcome.SUCCESS;

    /** Safe error message if outcome is FAILURE. */
    private String errorMessage;

    /** Correlation ID for distributed request tracing. Auto-resolved from MDC if null. */
    private String correlationId;

    /** Client IP address. Auto-resolved from MDC / HttpServletRequest if null. */
    private String clientIp;

    /** Client User-Agent. Auto-resolved from HttpServletRequest if null. */
    private String userAgent;

    /** Structured key-value metadata to be sanitized and converted to JSON. */
    private Map<String, Object> metadata;

    /** Raw metadata string (if already sanitized). */
    private String rawMetadata;
}
