package com.japaneselearning.module_auth.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.LoginResponse;
import com.japaneselearning.module_auth.dto.LogoutRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenResponse;
import com.japaneselearning.module_auth.dto.RegisterRequest;
import com.japaneselearning.module_auth.dto.RegisterResponse;
import com.japaneselearning.module_auth.entity.RefreshToken;
import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import com.japaneselearning.module_auth.util.JwtUtil;
import com.japaneselearning.module_auth.util.TokenHashUtil;
import java.util.Optional;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.RoleRepository;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        } else if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.LOGIN_FAILED, "Tài khoản không khả dụng");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenString = jwtUtil.generateRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.hashToken(refreshTokenString))
                .token(null)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User logged in successfully: {}", user.getEmail());

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .user(userInfo)
                .build();
    }

    @Override
    @Transactional(noRollbackFor = AppException.class)
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        if (requestRefreshToken == null || requestRefreshToken.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 1. Tìm token trong CSDL qua SHA-256 hash (hỗ trợ fallback legacy plaintext và tự động migrate)
        RefreshToken refreshTokenEntity = findRefreshToken(requestRefreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 2. Kiểm tra token đã bị thu hồi chưa (Token Reuse Detection)
        if (Boolean.TRUE.equals(refreshTokenEntity.getRevoked())) {
            log.warn("Token reuse detected! Revoking all tokens for user ID: {}", refreshTokenEntity.getUser().getId());
            revokeUserSessions(refreshTokenEntity.getUser().getId());
            throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        // 3. Kiểm tra token đã hết hạn theo CSDL chưa
        if (refreshTokenEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 4. Giải mã token qua JwtUtil
        String email;
        try {
            email = jwtUtil.extractRefreshEmail(requestRefreshToken);
        } catch (Exception e) {
            log.error("Failed to extract email from refresh token", e);
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 5. Kiểm tra tính hợp lệ của token (chữ ký, expiration từ bản thân token)
        if (!jwtUtil.isRefreshTokenValid(requestRefreshToken, email)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 6. Tìm User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 7. Kiểm tra trạng thái User
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        // 8. Thu hồi token cũ (Refresh Token Rotation)
        refreshTokenEntity.setRevoked(true);
        refreshTokenRepository.save(refreshTokenEntity);

        // 9. Cấp phát token mới (hash token khi lưu CSDL, không lưu plaintext)
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshTokenString = jwtUtil.generateRefreshToken(user);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHashUtil.hashToken(newRefreshTokenString))
                .token(null)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        log.info("Access token and refresh token rotated for user: {}", user.getEmail());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        if (requestRefreshToken == null || requestRefreshToken.isBlank()) {
            return;
        }

        // 1. Tìm token trong CSDL qua hash (hoặc legacy plaintext)
        findRefreshToken(requestRefreshToken)
                .ifPresent(refreshToken -> {
                    // 2. Đánh dấu đã bị thu hồi (revoke)
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                    log.info("Refresh token revoked for user: {}", refreshToken.getUser().getEmail());
                });
        
        // Nếu không tìm thấy, hệ thống cứ coi như logout thành công (Idempotent)
    }

    @Override
    @Transactional
    public int revokeUserSessions(Long userId) {
        if (userId == null) {
            return 0;
        }
        int revokedCount = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked {} active refresh token session(s) for user ID: {}", revokedCount, userId);
        return revokedCount;
    }


    private Optional<RefreshToken> findRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        Optional<RefreshToken> byHash = refreshTokenRepository.findByTokenHash(tokenHash);
        if (byHash.isPresent()) {
            return byHash;
        }
        // Fallback for legacy / pre-migration records:
        Optional<RefreshToken> legacy = refreshTokenRepository.findByToken(rawToken);
        if (legacy.isPresent()) {
            RefreshToken entity = legacy.get();
            // Upgrade legacy token on-the-fly: hash it and clear plaintext
            entity.setTokenHash(tokenHash);
            entity.setToken(null);
            return Optional.of(refreshTokenRepository.save(entity));
        }
        return Optional.empty();
    }
}
