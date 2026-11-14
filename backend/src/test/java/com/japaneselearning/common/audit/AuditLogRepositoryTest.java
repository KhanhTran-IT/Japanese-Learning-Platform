package com.japaneselearning.common.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AuditLogRepository Append-Only Tests")
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("Saves audit log successfully and persists immutable records")
    void save_PersistsAuditLog() {
        AuditLog log = AuditLog.builder()
                .actorId(10L)
                .actorEmail("admin@example.com")
                .actorRole("ADMIN")
                .action(AuditAction.USER_LOCK)
                .targetType("USER")
                .targetId("20")
                .outcome(AuditOutcome.SUCCESS)
                .correlationId("corr-123")
                .clientIp("127.0.0.1")
                .userAgent("Mozilla/5.0")
                .metadata("{\"reason\":\"Abuse detected\"}")
                .createdAt(LocalDateTime.now())
                .build();

        AuditLog saved = auditLogRepository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAction()).isEqualTo(AuditAction.USER_LOCK);
        assertThat(saved.getActorEmail()).isEqualTo("admin@example.com");

        List<AuditLog> all = auditLogRepository.findAll();
        assertThat(all).hasSize(1);
    }

    @Test
    @DisplayName("Queries audit logs with dynamic specification filtering")
    void findAll_WithSpecification_FiltersCorrectly() {
        AuditLog log1 = AuditLog.builder()
                .actorId(1L)
                .actorEmail("alice@example.com")
                .action(AuditAction.AUTH_LOGIN)
                .outcome(AuditOutcome.SUCCESS)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        AuditLog log2 = AuditLog.builder()
                .actorId(2L)
                .actorEmail("bob@example.com")
                .action(AuditAction.COURSE_CREATE)
                .outcome(AuditOutcome.SUCCESS)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        auditLogRepository.saveAll(List.of(log1, log2));

        Specification<AuditLog> spec = (root, query, cb) -> cb.equal(root.get("action"), AuditAction.AUTH_LOGIN);
        Page<AuditLog> page = auditLogRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getActorEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Append-only enforcement: deleteById throws UnsupportedOperationException")
    void deleteById_ThrowsUnsupportedOperationException() {
        assertThatThrownBy(() -> auditLogRepository.deleteById(1L))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Audit logs are append-only");
    }

    @Test
    @DisplayName("Append-only enforcement: delete throws UnsupportedOperationException")
    void delete_ThrowsUnsupportedOperationException() {
        AuditLog log = AuditLog.builder()
                .action(AuditAction.AUTH_LOGOUT)
                .outcome(AuditOutcome.SUCCESS)
                .build();

        assertThatThrownBy(() -> auditLogRepository.delete(log))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Audit logs are append-only");
    }

    @Test
    @DisplayName("Append-only enforcement: deleteAll throws UnsupportedOperationException")
    void deleteAll_ThrowsUnsupportedOperationException() {
        assertThatThrownBy(() -> auditLogRepository.deleteAll())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Audit logs are append-only");
    }
}
