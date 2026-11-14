package com.japaneselearning.common.audit;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Filter criteria for querying audit logs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilterReq {

    private Long actorId;

    @Size(max = 150)
    private String actorEmail;

    private AuditAction action;

    @Size(max = 64)
    private String targetType;

    @Size(max = 64)
    private String targetId;

    private AuditOutcome outcome;

    @Size(max = 64)
    private String correlationId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;
}
