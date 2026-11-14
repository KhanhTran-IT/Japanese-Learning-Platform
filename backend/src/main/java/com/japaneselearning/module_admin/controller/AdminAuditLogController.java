package com.japaneselearning.module_admin.controller;

import com.japaneselearning.common.audit.AuditAction;
import com.japaneselearning.common.audit.AuditLogFilterReq;
import com.japaneselearning.common.audit.AuditLogRes;
import com.japaneselearning.common.audit.AuditOutcome;
import com.japaneselearning.common.audit.AuditService;
import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Admin controller for querying the append-only audit trail.
 * <p>
 * Strictly read-only: no create, update, or delete operations are exposed.
 */
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin Audit Logs", description = "Read-only access to system security and business audit trail")
public class AdminAuditLogController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Query audit trail logs", description = "Allows administrators to search audit logs with multi-field filtering and pagination")
    public ApiResponse<PageResponse<AuditLogRes>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) AuditOutcome outcome,
            @RequestParam(required = false) String correlationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (page < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ số trang không được âm");
        }
        if (size <= 0 || size > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Kích thước trang phải từ 1 đến 100");
        }

        AuditLogFilterReq filter = AuditLogFilterReq.builder()
                .actorId(actorId)
                .actorEmail(actorEmail)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .outcome(outcome)
                .correlationId(correlationId)
                .from(from)
                .to(to)
                .build();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogRes> logPage = auditService.queryLogs(filter, pageable);

        PageResponse<AuditLogRes> response = PageResponse.<AuditLogRes>builder()
                .currentPage(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalPages(logPage.getTotalPages())
                .totalElements(logPage.getTotalElements())
                .data(logPage.getContent())
                .build();

        return ApiResponse.success("Lấy danh sách nhật ký kiểm toán thành công", response);
    }
}
