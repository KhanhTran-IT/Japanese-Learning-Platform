package com.japaneselearning.common.audit;

import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditServiceImpl Unit Tests")
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditDataRedactor dataRedactor;

    @Mock
    private UserRepository userRepository;

    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditLogRepository, dataRedactor, userRepository);
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("record() enriches event with MDC correlationId and security context user")
    void record_EnrichesEvent_FromSecurityContextAndMdc() {
        MDC.put("correlationId", "corr-test-999");

        User user = User.builder()
                .id(42L)
                .email("admin@test.com")
                .roles(Set.of(Role.builder().name(RoleName.ADMIN).build()))
                .build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin@test.com", "credentials", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        SecurityContextHolder.setContext(context);

        when(dataRedactor.toJsonString(any())).thenReturn("{\"param\":\"safe\"}");

        auditService.record(AuditAction.USER_LOCK, "USER", "100", AuditOutcome.SUCCESS, Map.of("param", "safe"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getActorId()).isEqualTo(42L);
        assertThat(saved.getActorEmail()).isEqualTo("admin@test.com");
        assertThat(saved.getActorRole()).contains("ROLE_ADMIN");
        assertThat(saved.getAction()).isEqualTo(AuditAction.USER_LOCK);
        assertThat(saved.getTargetType()).isEqualTo("USER");
        assertThat(saved.getTargetId()).isEqualTo("100");
        assertThat(saved.getOutcome()).isEqualTo(AuditOutcome.SUCCESS);
        assertThat(saved.getCorrelationId()).isEqualTo("corr-test-999");
        assertThat(saved.getMetadata()).isEqualTo("{\"param\":\"safe\"}");
    }

    @Test
    @DisplayName("record() handles anonymous user without throwing exception")
    void record_AnonymousUser_RecordsGracefully() {
        when(dataRedactor.toJsonString(any())).thenReturn(null);

        auditService.record(AuditAction.AUTH_LOGIN, "AUTH", "user@example.com", AuditOutcome.FAILURE,
                "Bad credentials", Map.of("attempt", 1));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getActorId()).isNull();
        assertThat(saved.getActorEmail()).isNull();
        assertThat(saved.getActorRole()).isEqualTo("ANONYMOUS");
        assertThat(saved.getOutcome()).isEqualTo(AuditOutcome.FAILURE);
        assertThat(saved.getErrorMessage()).isEqualTo("Bad credentials");
        assertThat(saved.getCorrelationId()).isNotNull(); // Auto-generated UUID
    }

    @Test
    @DisplayName("record() never fails caller even if repository throws exception")
    void record_RepositoryThrowsException_SuppressedSafely() {
        doThrow(new RuntimeException("DB Connection Timeout")).when(auditLogRepository).save(any());

        assertThatCode(() -> auditService.record(
                AuditEvent.builder()
                        .action(AuditAction.COURSE_CREATE)
                        .outcome(AuditOutcome.SUCCESS)
                        .build()
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("recordAsync() delegates persistence properly")
    void recordAsync_DelegatesPersistence() {
        AuditEvent event = AuditEvent.builder()
                .action(AuditAction.AUTH_LOGIN)
                .actorEmail("student@test.com")
                .outcome(AuditOutcome.SUCCESS)
                .build();

        auditService.recordAsync(event);

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
