package com.japaneselearning.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.module_admin.service.AdminUserService;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.LoginResponse;
import com.japaneselearning.module_auth.service.AuthService;
import com.japaneselearning.module_user.dto.ChangePasswordReq;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.RoleRepository;
import com.japaneselearning.module_user.repository.UserRepository;
import com.japaneselearning.module_user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Audit Trail End-to-End Security Integration Tests")
public class AuditTrailSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private User targetStudent;

    @BeforeEach
    void setUp() {
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.STUDENT).build()));
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));

        targetStudent = userRepository.save(User.builder()
                .email("student_audit@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Student Audit Test")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build());
    }

    @Test
    @DisplayName("Admin Audit API: Unauthenticated request returns 401 Unauthorized")
    void getAuditLogs_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin Audit API: Student access returns 403 Forbidden")
    @WithMockUser(roles = "STUDENT")
    void getAuditLogs_AsStudent_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin Audit API: Teacher access returns 403 Forbidden")
    @WithMockUser(roles = "TEACHER")
    void getAuditLogs_AsTeacher_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin Audit API: Admin access returns 200 OK with paginated logs")
    @WithMockUser(roles = "ADMIN")
    void getAuditLogs_AsAdmin_ReturnsOk() throws Exception {
        auditLogRepository.save(AuditLog.builder()
                .actorEmail("system@example.com")
                .actorRole("ADMIN")
                .action(AuditAction.USER_LOCK)
                .outcome(AuditOutcome.SUCCESS)
                .build());

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data").isArray())
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    @DisplayName("Login action generates AUTH_LOGIN audit trail entry with redacted metadata")
    void login_GeneratesAuditLog_WithNoPasswordInMetadata() {
        LoginRequest req = new LoginRequest();
        req.setEmail("student_audit@example.com");
        req.setPassword("Password123!");

        LoginResponse res = authService.login(req);
        assertThat(res).isNotNull();

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).anyMatch(log ->
                log.getAction() == AuditAction.AUTH_LOGIN &&
                log.getOutcome() == AuditOutcome.SUCCESS &&
                "student_audit@example.com".equals(log.getActorEmail())
        );

        // Verify password never leaked into any audit metadata
        for (AuditLog log : logs) {
            if (log.getMetadata() != null) {
                assertThat(log.getMetadata()).doesNotContain("Password123!");
            }
        }
    }

    @Test
    @DisplayName("Admin lock user generates USER_LOCK audit trail with target user ID")
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void lockUser_GeneratesAuditLog() {
        adminUserService.lockUser(targetStudent.getId());

        List<AuditLog> logs = auditLogRepository.findAll();
        assertThat(logs).anyMatch(log ->
                log.getAction() == AuditAction.USER_LOCK &&
                log.getOutcome() == AuditOutcome.SUCCESS &&
                String.valueOf(targetStudent.getId()).equals(log.getTargetId())
        );
    }
}
