package com.japaneselearning.common.observability;

import com.japaneselearning.module_auth.util.JwtUtil;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.RoleRepository;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Actuator Security & Access Control Integration Tests")
public class ActuatorSecurityAccessControlIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.monitoring.secret:test-monitoring-secret-key-32chars-min!}")
    private String monitoringSecret;

    private Role studentRole;
    private Role teacherRole;
    private Role adminRole;
    private Role superAdminRole;

    private String studentToken;
    private String teacherToken;
    private String adminToken;
    private String superAdminToken;

    @BeforeEach
    void setUp() {
        clearSecurityContext();
        userRepository.deleteAll();

        studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.STUDENT).build()));
        teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.TEACHER).build()));
        adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));
        superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.SUPER_ADMIN).build()));

        User student = createUser("student_actuator@example.com", studentRole);
        studentToken = jwtUtil.generateAccessToken(student);

        User teacher = createUser("teacher_actuator@example.com", teacherRole);
        teacherToken = jwtUtil.generateAccessToken(teacher);

        User admin = createUser("admin_actuator@example.com", adminRole);
        adminToken = jwtUtil.generateAccessToken(admin);

        User superAdmin = createUser("superadmin_actuator@example.com", superAdminRole);
        superAdminToken = jwtUtil.generateAccessToken(superAdmin);
    }

    @AfterEach
    void tearDown() {
        clearSecurityContext();
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        TestSecurityContextHolder.clearContext();
    }

    private User createUser(String email, Role role) {
        User user = User.builder()
                .email(email)
                .fullName("Test User " + email)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .status(UserStatus.ACTIVE)
                .tokenVersion(1)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        return userRepository.save(user);
    }

    // =========================================================================
    // 1. Public Health Probes (PermitAll)
    // =========================================================================

    @Test
    @DisplayName("Public Health: GET /actuator/health should be publicly accessible without authentication")
    void health_PublicAccess_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("Public Health: GET /actuator/health/liveness should be publicly accessible without authentication")
    void liveness_PublicAccess_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Public Health: GET /actuator/health/readiness should be publicly accessible without authentication")
    void readiness_PublicAccess_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // =========================================================================
    // 2. Unauthenticated Protected Actuator Access (Returns 401)
    // =========================================================================

    @Test
    @DisplayName("Security: GET /actuator/prometheus without auth should return 401 Unauthorized")
    void prometheus_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1006));
    }

    @Test
    @DisplayName("Security: GET /actuator/metrics without auth should return 401 Unauthorized")
    void metrics_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1006));
    }

    @Test
    @DisplayName("Security: GET /actuator/metrics/jvm.memory.used without auth should return 401 Unauthorized")
    void metricsDetail_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/actuator/metrics/jvm.memory.used"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1006));
    }

    @Test
    @DisplayName("Security: GET /actuator/info without auth should return 401 Unauthorized")
    void info_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1006));
    }

    @Test
    @DisplayName("Security: GET /actuator root without auth should return 401 Unauthorized")
    void actuatorRoot_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/actuator"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1006));
    }

    @Test
    @DisplayName("Security: GET /actuator/prometheus with invalid secret header should return 401 Unauthorized")
    void prometheus_InvalidSecretHeader_Returns401() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("X-Monitoring-Secret", "completely-invalid-secret-key-1234"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1006));
    }

    @Test
    @DisplayName("Security: GET /actuator/prometheus with invalid bearer token should return 401 Unauthorized")
    void prometheus_InvalidBearerToken_Returns401() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("Authorization", "Bearer invalid-or-malformed-token"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 3. Forbidden Role Access (Returns 403)
    // =========================================================================

    @Test
    @DisplayName("Security: GET /actuator/prometheus with STUDENT role should return 403 Forbidden")
    void prometheus_StudentRole_Returns403() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(3003));
    }

    @Test
    @DisplayName("Security: GET /actuator/metrics with STUDENT role should return 403 Forbidden")
    void metrics_StudentRole_Returns403() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(3003));
    }

    @Test
    @DisplayName("Security: GET /actuator/prometheus with TEACHER role should return 403 Forbidden")
    void prometheus_TeacherRole_Returns403() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(3003));
    }

    // =========================================================================
    // 4. Internal Monitoring Secret Authentication (Returns 200)
    // =========================================================================

    @Test
    @DisplayName("Monitoring Auth: GET /actuator/prometheus with X-Monitoring-Secret header should return 200 OK")
    void prometheus_WithMonitoringHeader_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("X-Monitoring-Secret", monitoringSecret))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_used_bytes")));
    }

    @Test
    @DisplayName("Monitoring Auth: GET /actuator/prometheus with Authorization Bearer monitoring secret should return 200 OK")
    void prometheus_WithMonitoringBearer_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("Authorization", "Bearer " + monitoringSecret))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_used_bytes")));
    }

    @Test
    @DisplayName("Monitoring Auth: GET /actuator/metrics with X-Monitoring-Secret header should return 200 OK")
    void metrics_WithMonitoringHeader_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("X-Monitoring-Secret", monitoringSecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    @DisplayName("Monitoring Auth: GET /actuator/metrics with Authorization Bearer monitoring secret should return 200 OK")
    void metrics_WithMonitoringBearer_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", "Bearer " + monitoringSecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    // =========================================================================
    // 5. Administrator Role Access (Returns 200)
    // =========================================================================

    @Test
    @DisplayName("Admin Auth: GET /actuator/prometheus with ADMIN role JWT should return 200 OK")
    void prometheus_AdminRole_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_used_bytes")));
    }

    @Test
    @DisplayName("Admin Auth: GET /actuator/metrics with ADMIN role JWT should return 200 OK")
    void metrics_AdminRole_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    @DisplayName("Admin Auth: GET /actuator/prometheus with SUPER_ADMIN role JWT should return 200 OK")
    void prometheus_SuperAdminRole_Returns200() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_used_bytes")));
    }
}
