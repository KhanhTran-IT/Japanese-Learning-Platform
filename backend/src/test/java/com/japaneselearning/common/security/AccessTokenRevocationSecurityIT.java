package com.japaneselearning.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_admin.dto.UpdateUserRolesReq;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AccessTokenRevocationSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenRevocationService tokenRevocationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Role studentRole;
    private Role adminRole;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        clearSecurityContext();
        userRepository.deleteAll();

        studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.STUDENT).build()));

        adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));

        teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.TEACHER).build()));
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        TestSecurityContextHolder.clearContext();
    }

    private User createUser(String email, String rawPassword, Role role) {
        User user = User.builder()
                .email(email)
                .fullName("User " + email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE)
                .tokenVersion(1)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Security: Active user access token allows calling protected endpoint")
    void activeUser_AccessToken_Success() throws Exception {
        User student = createUser("student_ok@example.com", "Password123!", studentRole);
        LoginResponse login = authService.login(new LoginRequest(student.getEmail(), "Password123!"));
        String accessToken = login.getAccessToken();

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value("student_ok@example.com"));
    }

    @Test
    @DisplayName("Security: Account lock immediately revokes access token for subsequent requests")
    void accountLock_ImmediatelyRevokesAccessToken() throws Exception {
        User student = createUser("student_lock_target@example.com", "Password123!", studentRole);
        User admin = createUser("admin_locker@example.com", "Admin123!", adminRole);

        LoginResponse login = authService.login(new LoginRequest(student.getEmail(), "Password123!"));
        String studentAccessToken = login.getAccessToken();

        // 1. Initial request works
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + studentAccessToken))
                .andExpect(status().isOk());

        // 2. Admin locks student account
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), "Admin123!")
        );
        adminUserService.lockUser(student.getId());
        clearSecurityContext();

        // 3. Immediately, the access token is rejected
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + studentAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_TOKEN_REVOKED.getCode()));
    }

    @Test
    @DisplayName("Security: Account deletion immediately revokes access token")
    void accountDeletion_ImmediatelyRevokesAccessToken() throws Exception {
        User student = createUser("student_del_target@example.com", "Password123!", studentRole);
        User admin = createUser("admin_deleter@example.com", "Admin123!", adminRole);

        LoginResponse login = authService.login(new LoginRequest(student.getEmail(), "Password123!"));
        String studentAccessToken = login.getAccessToken();

        // Admin deletes student account
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), "Admin123!")
        );
        adminUserService.deleteUser(student.getId());
        clearSecurityContext();

        // Access token rejected immediately
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + studentAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_TOKEN_REVOKED.getCode()));
    }

    @Test
    @DisplayName("Security: Password change immediately invalidates old access token and increments version")
    void passwordChange_ImmediatelyInvalidatesOldAccessToken() throws Exception {
        User student = createUser("student_pwd_change@example.com", "OldPassword123!", studentRole);

        LoginResponse login = authService.login(new LoginRequest(student.getEmail(), "OldPassword123!"));
        String oldAccessToken = login.getAccessToken();

        // Student changes password
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(student.getEmail(), "OldPassword123!")
        );
        userService.changePassword(new ChangePasswordReq("OldPassword123!", "NewStrongPassword123!", "NewStrongPassword123!"));
        clearSecurityContext();

        // Old access token with version 1 is rejected immediately
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_TOKEN_REVOKED.getCode()));

        // Login with new password gives a new access token that works
        LoginResponse newLogin = authService.login(new LoginRequest(student.getEmail(), "NewStrongPassword123!"));
        String newAccessToken = newLogin.getAccessToken();

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security: Role change immediately invalidates existing access token")
    void roleChange_ImmediatelyInvalidatesExistingAccessToken() throws Exception {
        User student = createUser("student_promoted@example.com", "Password123!", studentRole);
        User admin = createUser("admin_promoter@example.com", "Admin123!", adminRole);

        LoginResponse login = authService.login(new LoginRequest(student.getEmail(), "Password123!"));
        String oldAccessToken = login.getAccessToken();

        // Admin updates student's roles to TEACHER
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), "Admin123!")
        );
        adminUserService.updateUserRoles(student.getId(), new UpdateUserRolesReq(Set.of(RoleName.TEACHER)));
        clearSecurityContext();

        // Old token is invalidated
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + oldAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_TOKEN_REVOKED.getCode()));

        // Fresh login issues new token with updated TEACHER role
        LoginResponse newLogin = authService.login(new LoginRequest(student.getEmail(), "Password123!"));
        String newAccessToken = newLogin.getAccessToken();

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.roles[0]").value("TEACHER"));
    }
}
