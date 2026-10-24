package com.japaneselearning.module_auth.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.LoginResponse;
import com.japaneselearning.module_auth.dto.LogoutRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenResponse;
import com.japaneselearning.module_auth.entity.RefreshToken;
import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import com.japaneselearning.module_auth.util.JwtUtil;
import com.japaneselearning.module_auth.util.TokenHashUtil;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.RoleRepository;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import com.japaneselearning.common.observability.ObservabilityMetrics;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ObservabilityMetrics observabilityMetrics;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder().id(1L).name(RoleName.STUDENT).build();
        activeUser = User.builder()
                .id(10L)
                .email("user@example.com")
                .fullName("User Test")
                .passwordHash("encoded-pwd")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(studentRole))
                .build();
    }

    @Test
    void login_Success_StoresHashedRefreshTokenAtRest() {
        LoginRequest request = new LoginRequest("user@example.com", "mypassword");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("mypassword", "encoded-pwd")).thenReturn(true);
        when(jwtUtil.generateAccessToken(activeUser)).thenReturn("raw-access-token");
        when(jwtUtil.generateRefreshToken(activeUser)).thenReturn("raw-refresh-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("raw-access-token", response.getAccessToken());
        assertEquals("raw-refresh-token", response.getRefreshToken());

        // Verify entity saved with tokenHash and null plaintext token
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken savedEntity = captor.getValue();

        assertNotNull(savedEntity.getTokenHash());
        assertEquals(TokenHashUtil.hashToken("raw-refresh-token"), savedEntity.getTokenHash());
        assertNull(savedEntity.getToken(), "Plaintext token must not be stored at rest");
        assertFalse(savedEntity.getRevoked());
    }

    @Test
    void refreshToken_Success_RotatesTokenAndStoresHashed() {
        String rawOldToken = "old-raw-token";
        String oldHash = TokenHashUtil.hashToken(rawOldToken);

        RefreshToken oldEntity = RefreshToken.builder()
                .id(1L)
                .tokenHash(oldHash)
                .token(null)
                .user(activeUser)
                .expiredAt(LocalDateTime.now().plusDays(5))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(oldHash)).thenReturn(Optional.of(oldEntity));
        when(jwtUtil.extractRefreshEmail(rawOldToken)).thenReturn("user@example.com");
        when(jwtUtil.isRefreshTokenValid(rawOldToken, "user@example.com")).thenReturn(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(jwtUtil.generateAccessToken(activeUser)).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(activeUser)).thenReturn("new-raw-refresh-token");

        RefreshTokenResponse response = authService.refreshToken(new RefreshTokenRequest(rawOldToken));

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-raw-refresh-token", response.getRefreshToken());

        // Check old token revoked
        assertTrue(oldEntity.getRevoked());

        // Check new token saved with hash and null plaintext
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        RefreshToken newSavedEntity = captor.getAllValues().get(1);

        assertEquals(TokenHashUtil.hashToken("new-raw-refresh-token"), newSavedEntity.getTokenHash());
        assertNull(newSavedEntity.getToken(), "New token plaintext must not be stored at rest");
    }

    @Test
    void refreshToken_ReuseDetected_RevokesAllTokensAndThrows() {
        String rawReusedToken = "reused-token";
        String hash = TokenHashUtil.hashToken(rawReusedToken);

        RefreshToken revokedEntity = RefreshToken.builder()
                .id(2L)
                .tokenHash(hash)
                .user(activeUser)
                .expiredAt(LocalDateTime.now().plusDays(5))
                .revoked(true) // already revoked -> REUSE!
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(revokedEntity));

        AppException ex = assertThrows(AppException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(rawReusedToken)));

        assertEquals(ErrorCode.REFRESH_TOKEN_REVOKED, ex.getErrorCode());
        // Verify family revocation
        verify(refreshTokenRepository).revokeAllByUserId(activeUser.getId());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshToken_ExpiredToken_ThrowsException() {
        String rawToken = "expired-token";
        String hash = TokenHashUtil.hashToken(rawToken);

        RefreshToken expiredEntity = RefreshToken.builder()
                .id(3L)
                .tokenHash(hash)
                .user(activeUser)
                .expiredAt(LocalDateTime.now().minusMinutes(5))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expiredEntity));

        AppException ex = assertThrows(AppException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(rawToken)));

        assertEquals(ErrorCode.REFRESH_TOKEN_EXPIRED, ex.getErrorCode());
    }

    @Test
    void refreshToken_LegacyPlaintextFallback_UpgradesToHash() {
        String legacyRawToken = "legacy-raw-token";
        String expectedHash = TokenHashUtil.hashToken(legacyRawToken);

        RefreshToken legacyEntity = RefreshToken.builder()
                .id(4L)
                .token(legacyRawToken)
                .tokenHash(null) // legacy row
                .user(activeUser)
                .expiredAt(LocalDateTime.now().plusDays(3))
                .revoked(false)
                .build();

        // Not in hash lookup
        when(refreshTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.empty());
        // Found in fallback plaintext lookup
        when(refreshTokenRepository.findByToken(legacyRawToken)).thenReturn(Optional.of(legacyEntity));
        when(refreshTokenRepository.save(legacyEntity)).thenReturn(legacyEntity);

        when(jwtUtil.extractRefreshEmail(legacyRawToken)).thenReturn("user@example.com");
        when(jwtUtil.isRefreshTokenValid(legacyRawToken, "user@example.com")).thenReturn(true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(jwtUtil.generateAccessToken(activeUser)).thenReturn("new-acc");
        when(jwtUtil.generateRefreshToken(activeUser)).thenReturn("new-ref");

        RefreshTokenResponse response = authService.refreshToken(new RefreshTokenRequest(legacyRawToken));

        assertNotNull(response);
        assertEquals("new-acc", response.getAccessToken());
        // Verifies the legacy entity had its tokenHash set and plaintext nulled during upgrade
        assertEquals(expectedHash, legacyEntity.getTokenHash());
        assertNull(legacyEntity.getToken());
    }

    @Test
    void logout_RevokesTokenByHash() {
        String rawToken = "logout-token";
        String hash = TokenHashUtil.hashToken(rawToken);

        RefreshToken entity = RefreshToken.builder()
                .id(5L)
                .tokenHash(hash)
                .user(activeUser)
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(entity));

        authService.logout(new LogoutRequest(rawToken));

        assertTrue(entity.getRevoked());
        verify(refreshTokenRepository).save(entity);
    }

    @Test
    void revokeUserSessions_Success_RevokesAllSessionsAndReturnsCount() {
        when(refreshTokenRepository.revokeAllByUserId(10L)).thenReturn(3);

        int count = authService.revokeUserSessions(10L);

        assertEquals(3, count);
        verify(refreshTokenRepository).revokeAllByUserId(10L);
    }

    @Test
    void revokeUserSessions_NullUserId_ReturnsZeroWithoutInvokingRepo() {
        int count = authService.revokeUserSessions(null);

        assertEquals(0, count);
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
}
