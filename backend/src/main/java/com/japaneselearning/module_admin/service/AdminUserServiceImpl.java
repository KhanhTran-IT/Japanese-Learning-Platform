package com.japaneselearning.module_admin.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_admin.dto.AdminUserRes;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import com.japaneselearning.module_auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final com.japaneselearning.common.security.TokenRevocationService tokenRevocationService;
    private final com.japaneselearning.module_user.repository.RoleRepository roleRepository;

    @Override
    public PageResponse<AdminUserRes> getUsers(String keyword, UserStatus status, RoleName role, Pageable pageable) {
        Page<User> userPage = userRepository.findUsersByCriteria(keyword, status, role, pageable);
        
        return PageResponse.<AdminUserRes>builder()
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .data(userPage.getContent().stream()
                        .map(this::mapToAdminUserRes)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public AdminUserRes getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToAdminUserRes(user);
    }

    @Override
    @Transactional
    public AdminUserRes lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validateActionOnUser(user, "khóa");

        user.setStatus(UserStatus.LOCKED);
        user = userRepository.save(user);

        // Revoke all active sessions so the locked user cannot refresh tokens or maintain active sessions
        authService.revokeUserSessions(user.getId());
        log.info("User ID {} locked and active sessions revoked", user.getId());

        return mapToAdminUserRes(user);
    }

    @Override
    @Transactional
    public AdminUserRes unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validateActionOnUser(user, "mở khóa");

        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        // Evict cached security state so unlock reflects immediately
        tokenRevocationService.evictUserSecurityState(user.getId());

        return mapToAdminUserRes(user);
    }

    @Override
    @Transactional
    public AdminUserRes deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validateActionOnUser(user, "xóa");

        user.setStatus(UserStatus.DELETED);
        user = userRepository.save(user);

        // Revoke active sessions and invalidate all access tokens
        authService.revokeUserSessions(user.getId());
        tokenRevocationService.revokeUserTokens(user.getId());
        log.info("User ID {} marked DELETED, active sessions revoked and access tokens invalidated", user.getId());

        return mapToAdminUserRes(user);
    }

    @Override
    @Transactional
    public AdminUserRes updateUserRoles(Long id, com.japaneselearning.module_admin.dto.UpdateUserRolesReq req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validateActionOnUser(user, "thay đổi quyền");

        java.util.Set<com.japaneselearning.module_user.entity.Role> newRoles = new java.util.HashSet<>();
        for (RoleName roleName : req.getRoles()) {
            com.japaneselearning.module_user.entity.Role r = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            newRoles.add(r);
        }

        user.setRoles(newRoles);
        user = userRepository.save(user);

        // When roles change, revoke active sessions and invalidate existing access tokens
        // so the user must obtain a fresh token with updated claims/authorities
        authService.revokeUserSessions(user.getId());
        tokenRevocationService.revokeUserTokens(user.getId());
        log.info("User ID {} roles updated to {}, active sessions revoked and tokens invalidated", user.getId(), req.getRoles());

        return mapToAdminUserRes(user);
    }

    private void validateActionOnUser(User targetUser, String actionName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            String currentUserEmail = auth.getName();
            if (currentUserEmail.equals(targetUser.getEmail())) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể " + actionName + " tài khoản của chính mình");
            }
        }

        boolean isSuperAdmin = targetUser.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.SUPER_ADMIN);
        
        if (isSuperAdmin) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể " + actionName + " tài khoản SUPER_ADMIN");
        }
    }

    private AdminUserRes mapToAdminUserRes(User user) {
        return AdminUserRes.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
