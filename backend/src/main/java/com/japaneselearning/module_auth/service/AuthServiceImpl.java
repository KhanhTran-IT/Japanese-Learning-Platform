package com.japaneselearning.module_auth.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.LoginResponse;
import com.japaneselearning.module_auth.dto.RegisterRequest;
import com.japaneselearning.module_auth.dto.RegisterResponse;
import com.japaneselearning.module_auth.entity.RefreshToken;
import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import com.japaneselearning.module_auth.util.JwtUtil;
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
        // 1. Kiểm tra confirmPassword khớp password
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        // 2. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 3. Lấy role STUDENT từ database
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // 4. Hash password bằng BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 5. Tạo user mới
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();

        // 6. Lưu vào database
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // 7. Trả response DTO (không trả Entity)
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
        // 1. Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));

        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        // 3. Kiểm tra status
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        } else if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.DELETED) {
            // Có thể dùng một mã lỗi riêng hoặc dùng chung
            throw new AppException(ErrorCode.LOGIN_FAILED, "Tài khoản không khả dụng");
        }

        // 4. Cập nhật last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenString = jwtUtil.generateRefreshToken(user);

        // 6. Lưu refresh token vào database
        // Trong hệ thống đơn giản: user chỉ có 1 device thì xóa hết token cũ rồi lưu token mới
        // Ở đây để đơn giản ta cứ lưu thêm vào (thực tế có thể clean up job)
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                // expiredAt tính từ util, ta cộng cứng 7 ngày vào cho đơn giản ở DB
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User logged in successfully: {}", user.getEmail());

        // 7. Trả về
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
}
