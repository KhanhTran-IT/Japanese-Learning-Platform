package com.japaneselearning.common.audit;

import com.japaneselearning.common.logging.CorrelationIdFilter;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AuditService} providing both synchronous (REQUIRES_NEW)
 * and asynchronous (thread-pooled) append-only persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditDataRedactor auditDataRedactor;
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEvent event) {
        persistEvent(event);
    }

    @Override
    @Async(AuditAsyncConfig.AUDIT_EXECUTOR_BEAN)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAsync(AuditEvent event) {
        persistEvent(event);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditAction action, String targetType, String targetId, AuditOutcome outcome, Map<String, Object> metadata) {
        record(action, targetType, targetId, outcome, null, metadata);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditAction action, String targetType, String targetId, AuditOutcome outcome, String errorMessage, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .outcome(outcome)
                .errorMessage(errorMessage)
                .metadata(metadata)
                .build();
        record(event);
    }

    @Override
    @Async(AuditAsyncConfig.AUDIT_EXECUTOR_BEAN)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAsync(AuditAction action, String targetType, String targetId, AuditOutcome outcome, Map<String, Object> metadata) {
        recordAsync(action, targetType, targetId, outcome, null, metadata);
    }

    @Override
    @Async(AuditAsyncConfig.AUDIT_EXECUTOR_BEAN)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAsync(AuditAction action, String targetType, String targetId, AuditOutcome outcome, String errorMessage, Map<String, Object> metadata) {
        AuditEvent event = AuditEvent.builder()
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .outcome(outcome)
                .errorMessage(errorMessage)
                .metadata(metadata)
                .build();
        recordAsync(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogRes> queryLogs(AuditLogFilterReq filter, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getActorId() != null) {
                predicates.add(cb.equal(root.get("actorId"), filter.getActorId()));
            }
            if (filter.getActorEmail() != null && !filter.getActorEmail().isBlank()) {
                predicates.add(cb.equal(root.get("actorEmail"), filter.getActorEmail().trim()));
            }
            if (filter.getAction() != null) {
                predicates.add(cb.equal(root.get("action"), filter.getAction()));
            }
            if (filter.getTargetType() != null && !filter.getTargetType().isBlank()) {
                predicates.add(cb.equal(root.get("targetType"), filter.getTargetType().trim()));
            }
            if (filter.getTargetId() != null && !filter.getTargetId().isBlank()) {
                predicates.add(cb.equal(root.get("targetId"), filter.getTargetId().trim()));
            }
            if (filter.getOutcome() != null) {
                predicates.add(cb.equal(root.get("outcome"), filter.getOutcome()));
            }
            if (filter.getCorrelationId() != null && !filter.getCorrelationId().isBlank()) {
                predicates.add(cb.equal(root.get("correlationId"), filter.getCorrelationId().trim()));
            }
            if (filter.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFrom()));
            }
            if (filter.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable).map(this::mapToAuditLogRes);
    }

    private void persistEvent(AuditEvent event) {
        if (event == null) {
            return;
        }

        try {
            enrichEventContext(event);

            String sanitizedMetadataJson = event.getRawMetadata();
            if (sanitizedMetadataJson == null && event.getMetadata() != null) {
                sanitizedMetadataJson = auditDataRedactor.toJsonString(event.getMetadata());
            }

            String safeErrorMessage = event.getErrorMessage();
            if (safeErrorMessage != null && safeErrorMessage.length() > 500) {
                safeErrorMessage = safeErrorMessage.substring(0, 500);
            }

            AuditLog logEntry = AuditLog.builder()
                    .actorId(event.getActorId())
                    .actorEmail(event.getActorEmail())
                    .actorRole(event.getActorRole())
                    .action(event.getAction())
                    .targetType(event.getTargetType())
                    .targetId(event.getTargetId())
                    .outcome(event.getOutcome() != null ? event.getOutcome() : AuditOutcome.SUCCESS)
                    .errorMessage(safeErrorMessage)
                    .correlationId(event.getCorrelationId())
                    .clientIp(event.getClientIp())
                    .userAgent(event.getUserAgent())
                    .metadata(sanitizedMetadataJson)
                    .build();

            auditLogRepository.save(logEntry);
            log.debug("Persisted audit log [id={}, action={}, outcome={}, correlationId={}]",
                    logEntry.getId(), logEntry.getAction(), logEntry.getOutcome(), logEntry.getCorrelationId());

        } catch (Exception e) {
            log.error("Failed to persist audit log for action {}: {}", event.getAction(), e.getMessage(), e);
        }
    }

    /**
     * Auto-enrich missing contextual metadata from SecurityContext, MDC, and current HTTP request.
     */
    private void enrichEventContext(AuditEvent event) {
        // 1. Correlation ID from MDC or auto-generate fallback
        if (event.getCorrelationId() == null || event.getCorrelationId().isBlank()) {
            String mdcCorrelationId = MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID);
            if (mdcCorrelationId != null && !mdcCorrelationId.isBlank()) {
                event.setCorrelationId(mdcCorrelationId);
            } else {
                event.setCorrelationId(UUID.randomUUID().toString());
            }
        }

        // 2. HTTP Request details (Client IP, User Agent)
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            if (event.getClientIp() == null || event.getClientIp().isBlank()) {
                String ip = MDC.get(CorrelationIdFilter.MDC_CLIENT_IP);
                if (ip == null || ip.isBlank()) {
                    ip = request.getRemoteAddr();
                }
                event.setClientIp(ip);
            }

            if (event.getUserAgent() == null || event.getUserAgent().isBlank()) {
                String ua = request.getHeader("User-Agent");
                if (ua != null && ua.length() > 255) {
                    ua = ua.substring(0, 255);
                }
                event.setUserAgent(ua);
            }
        }

        // 3. Actor details from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            if (event.getActorEmail() == null || event.getActorEmail().isBlank()) {
                event.setActorEmail(auth.getName());
            }

            if (event.getActorRole() == null || event.getActorRole().isBlank()) {
                String roles = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
                event.setActorRole(roles);
            }

            if (event.getActorId() == null && event.getActorEmail() != null) {
                userRepository.findByEmail(event.getActorEmail())
                        .map(User::getId)
                        .ifPresent(event::setActorId);
            }
        } else if (event.getActorRole() == null || event.getActorRole().isBlank()) {
            event.setActorRole("ANONYMOUS");
        }

        if (event.getActorId() != null && (event.getActorEmail() == null || event.getActorEmail().isBlank())) {
            userRepository.findById(event.getActorId())
                    .map(User::getEmail)
                    .ifPresent(event::setActorEmail);
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private AuditLogRes mapToAuditLogRes(AuditLog entity) {
        return AuditLogRes.builder()
                .id(entity.getId())
                .actorId(entity.getActorId())
                .actorEmail(entity.getActorEmail())
                .actorRole(entity.getActorRole())
                .action(entity.getAction())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .outcome(entity.getOutcome())
                .errorMessage(entity.getErrorMessage())
                .correlationId(entity.getCorrelationId())
                .clientIp(entity.getClientIp())
                .userAgent(entity.getUserAgent())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
