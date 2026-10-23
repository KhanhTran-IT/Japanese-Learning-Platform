package com.japaneselearning.module_user.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_user.dto.ChangePasswordReq;
import com.japaneselearning.module_user.dto.CurrentUserResponse;
import com.japaneselearning.module_user.dto.UpdateCurrentUserReq;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import com.japaneselearning.module_auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;


    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        User user = getAuthenticatedUser();
        return toCurrentUserResponse(user);
    }

    @Override
    @Transactional
    public CurrentUserResponse updateCurrentUser(UpdateCurrentUserReq request) {
        User user = getAuthenticatedUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getIsPrivateLeaderboard() != null) {
            user.setIsPrivateLeaderboard(request.getIsPrivateLeaderboard());
        }

        userRepository.save(user);
        return toCurrentUserResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordReq request) {
        User user = getAuthenticatedUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        // Verify confirm password matches new password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        // Encode and save new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all active sessions on other devices / clients after password change
        authService.revokeUserSessions(user.getId());
        log.info("Password changed and active sessions revoked for user: {}", user.getEmail());
    }

    // --- Private Helpers ---

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private CurrentUserResponse toCurrentUserResponse(User user) {
        return CurrentUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .country(user.getCountry())
                .isPrivateLeaderboard(user.getIsPrivateLeaderboard())
                .level(user.getLevel())
                .currentStreak(user.getCurrentStreak())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .build();
    }
}
