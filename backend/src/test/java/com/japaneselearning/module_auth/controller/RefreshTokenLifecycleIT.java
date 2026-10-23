package com.japaneselearning.module_auth.controller;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_admin.dto.AdminUserRes;
import com.japaneselearning.module_admin.service.AdminUserService;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.LoginResponse;
import com.japaneselearning.module_auth.dto.RefreshTokenRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenResponse;
import com.japaneselearning.module_auth.entity.RefreshToken;
import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import com.japaneselearning.module_auth.service.AuthService;
import com.japaneselearning.module_auth.service.TokenCleanupScheduler;
import com.japaneselearning.module_auth.util.TokenHashUtil;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RefreshTokenLifecycleIT {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenCleanupScheduler tokenCleanupScheduler;

    private Role studentRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.STUDENT).build()));

        adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));
    }

    private User createAndSaveUser(String email, String rawPassword, Role role) {
        User user = User.builder()
                .email(email)
                .fullName("Test User " + email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Scenario 1: Refresh Token Rotation & Token Reuse Detection")
    void refreshTokenRotation_AndTokenReuseDetection() {
        User user = createAndSaveUser("student_rotate@example.com", "Password123!", studentRole);

        // 1. Login to get initial refresh token
        LoginResponse loginRes = authService.login(new LoginRequest(user.getEmail(), "Password123!"));
        String initialRefreshToken = loginRes.getRefreshToken();
        String initialHash = TokenHashUtil.hashToken(initialRefreshToken);

        RefreshToken savedInitial = refreshTokenRepository.findByTokenHash(initialHash).orElseThrow();
        assertThat(savedInitial.getRevoked()).isFalse();
        assertThat(savedInitial.getToken()).isNull(); // Zero plaintext at rest

        // 2. Perform rotation
        RefreshTokenResponse rotateRes = authService.refreshToken(new RefreshTokenRequest(initialRefreshToken));
        String newRefreshToken = rotateRes.getRefreshToken();
        String newHash = TokenHashUtil.hashToken(newRefreshToken);

        assertThat(newRefreshToken).isNotEqualTo(initialRefreshToken);

        // Verify old token is revoked
        RefreshToken oldTokenInDb = refreshTokenRepository.findByTokenHash(initialHash).orElseThrow();
        assertThat(oldTokenInDb.getRevoked()).isTrue();

        // Verify new token is active
        RefreshToken newTokenInDb = refreshTokenRepository.findByTokenHash(newHash).orElseThrow();
        assertThat(newTokenInDb.getRevoked()).isFalse();

        // 3. Token Reuse Attempt (Replay attack with old token)
        AppException ex = assertThrows(AppException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(initialRefreshToken)));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);

        // Verify entire token family was revoked upon reuse detection
        RefreshToken reusedNewToken = refreshTokenRepository.findByTokenHash(newHash).orElseThrow();
        assertThat(reusedNewToken.getRevoked()).isTrue();

        // New token cannot be used anymore
        AppException ex2 = assertThrows(AppException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(newRefreshToken)));
        assertThat(ex2.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);
    }

    @Test
    @DisplayName("Scenario 2: Scheduled Cleanup is transactional, repeatable, and preserves active sessions")
    void scheduledCleanup_PrunesExpiredAndRevoked_PreservesActiveSessions() {
        User user = createAndSaveUser("student_cleanup@example.com", "Password123!", studentRole);
        LocalDateTime now = LocalDateTime.now();

        // Token 1: ACTIVE (revoked = false, expires in 7 days)
        RefreshToken activeToken = RefreshToken.builder()
                .user(user)
                .tokenHash("hash_active_session")
                .expiredAt(now.plusDays(7))
                .revoked(false)
                .build();

        // Token 2: EXPIRED (revoked = false, expired 2 days ago)
        RefreshToken expiredToken = RefreshToken.builder()
                .user(user)
                .tokenHash("hash_expired_session")
                .expiredAt(now.minusDays(2))
                .revoked(false)
                .build();

        // Token 3: REVOKED (revoked = true, expires in 5 days)
        RefreshToken revokedToken = RefreshToken.builder()
                .user(user)
                .tokenHash("hash_revoked_session")
                .expiredAt(now.plusDays(5))
                .revoked(true)
                .build();

        // Token 4: EXPIRED & REVOKED (revoked = true, expired 10 days ago)
        RefreshToken expiredAndRevokedToken = RefreshToken.builder()
                .user(user)
                .tokenHash("hash_expired_and_revoked")
                .expiredAt(now.minusDays(10))
                .revoked(true)
                .build();

        refreshTokenRepository.saveAll(List.of(activeToken, expiredToken, revokedToken, expiredAndRevokedToken));
        assertThat(refreshTokenRepository.count()).isEqualTo(4);

        // 1. Run cleanup
        int deletedFirstRun = tokenCleanupScheduler.cleanupExpiredOrRevokedTokens();
        assertThat(deletedFirstRun).isEqualTo(3);

        // Verify only activeToken survives
        List<RefreshToken> remaining = refreshTokenRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getTokenHash()).isEqualTo("hash_active_session");
        assertThat(remaining.get(0).getRevoked()).isFalse();

        // 2. Repeatable check (running again leaves active session untouched and deletes 0 rows)
        int deletedSecondRun = tokenCleanupScheduler.cleanupExpiredOrRevokedTokens();
        assertThat(deletedSecondRun).isEqualTo(0);

        List<RefreshToken> remainingSecond = refreshTokenRepository.findAll();
        assertThat(remainingSecond).hasSize(1);
        assertThat(remainingSecond.get(0).getTokenHash()).isEqualTo("hash_active_session");
    }

    @Test
    @DisplayName("Scenario 3: Session Revocation after Password Change")
    void passwordChangeFlow_RevokesAllActiveSessions() {
        User user = createAndSaveUser("student_pwd@example.com", "CurrentSecret123!", studentRole);

        // User logs in on 2 devices
        LoginResponse device1 = authService.login(new LoginRequest(user.getEmail(), "CurrentSecret123!"));
        LoginResponse device2 = authService.login(new LoginRequest(user.getEmail(), "CurrentSecret123!"));

        String token1 = device1.getRefreshToken();
        String token2 = device2.getRefreshToken();

        String hash1 = TokenHashUtil.hashToken(token1);
        String hash2 = TokenHashUtil.hashToken(token2);

        assertThat(refreshTokenRepository.findByTokenHash(hash1).orElseThrow().getRevoked()).isFalse();
        assertThat(refreshTokenRepository.findByTokenHash(hash2).orElseThrow().getRevoked()).isFalse();

        // Set security context for student
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), "CurrentSecret123!")
        );

        // Failed attempt: wrong password does NOT revoke sessions
        assertThrows(AppException.class, () ->
                userService.changePassword(new ChangePasswordReq("WrongPassword!", "NewPass123!", "NewPass123!")));

        assertThat(refreshTokenRepository.findByTokenHash(hash1).orElseThrow().getRevoked()).isFalse();
        assertThat(refreshTokenRepository.findByTokenHash(hash2).orElseThrow().getRevoked()).isFalse();

        // Successful password change
        userService.changePassword(new ChangePasswordReq("CurrentSecret123!", "NewStrongPass123!", "NewStrongPass123!"));

        // Verify in DB that both sessions are now REVOKED
        assertThat(refreshTokenRepository.findByTokenHash(hash1).orElseThrow().getRevoked()).isTrue();
        assertThat(refreshTokenRepository.findByTokenHash(hash2).orElseThrow().getRevoked()).isTrue();

        // Subsequent refresh attempts with old sessions fail
        AppException ex1 = assertThrows(AppException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(token1)));
        assertThat(ex1.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);

        AppException ex2 = assertThrows(AppException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(token2)));
        assertThat(ex2.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED);

        // Logging in with old password fails
        assertThrows(AppException.class, () ->
                authService.login(new LoginRequest(user.getEmail(), "CurrentSecret123!")));

        // Logging in with new password succeeds and creates a fresh active session
        LoginResponse newLogin = authService.login(new LoginRequest(user.getEmail(), "NewStrongPass123!"));
        assertThat(newLogin.getAccessToken()).isNotNull();

        String newHash = TokenHashUtil.hashToken(newLogin.getRefreshToken());
        assertThat(refreshTokenRepository.findByTokenHash(newHash).orElseThrow().getRevoked()).isFalse();
    }

    @Test
    @DisplayName("Scenario 4: Session Revocation after Account Lock")
    void accountLockFlow_RevokesAllActiveSessions() {
        User student = createAndSaveUser("student_locked@example.com", "Password123!", studentRole);
        User admin = createAndSaveUser("admin_lock_action@example.com", "AdminPass123!", adminRole);

        // Student logs in
        LoginResponse loginRes = authService.login(new LoginRequest(student.getEmail(), "Password123!"));
        String refreshToken = loginRes.getRefreshToken();
        String tokenHash = TokenHashUtil.hashToken(refreshToken);

        assertThat(refreshTokenRepository.findByTokenHash(tokenHash).orElseThrow().getRevoked()).isFalse();

        // Authenticate as Admin
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), "AdminPass123!")
        );

        // Admin locks student account
        AdminUserRes lockRes = adminUserService.lockUser(student.getId());
        assertThat(lockRes.getStatus()).isEqualTo(UserStatus.LOCKED);

        // Verify student status in DB
        User lockedStudentInDb = userRepository.findById(student.getId()).orElseThrow();
        assertThat(lockedStudentInDb.getStatus()).isEqualTo(UserStatus.LOCKED);

        // Verify session was revoked in DB
        RefreshToken sessionInDb = refreshTokenRepository.findByTokenHash(tokenHash).orElseThrow();
        assertThat(sessionInDb.getRevoked()).isTrue();

        // Attempting to refresh fails
        assertThrows(AppException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(refreshToken)));

        // Attempting to log in fails with ACCOUNT_LOCKED
        AppException loginEx = assertThrows(AppException.class, () ->
                authService.login(new LoginRequest(student.getEmail(), "Password123!")));
        assertThat(loginEx.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LOCKED);

        // Admin unlocks student
        AdminUserRes unlockRes = adminUserService.unlockUser(student.getId());
        assertThat(unlockRes.getStatus()).isEqualTo(UserStatus.ACTIVE);

        // Old token remains revoked
        assertThrows(AppException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(refreshToken)));

        // Student can now log in cleanly with their password
        LoginResponse freshLogin = authService.login(new LoginRequest(student.getEmail(), "Password123!"));
        assertThat(freshLogin.getAccessToken()).isNotNull();
    }
}
