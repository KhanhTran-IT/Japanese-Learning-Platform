package com.japaneselearning.common.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read-only DTO for presenting audit trail logs to authorized administrators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRes {

    private Long id;
    private Long actorId;
    private String actorEmail;
    private String actorRole;
    private AuditAction action;
    private String targetType;
    private String targetId;
    private AuditOutcome outcome;
    private String errorMessage;
    private String correlationId;
    private String clientIp;
    private String userAgent;
    private String metadata;
    private LocalDateTime createdAt;
}
