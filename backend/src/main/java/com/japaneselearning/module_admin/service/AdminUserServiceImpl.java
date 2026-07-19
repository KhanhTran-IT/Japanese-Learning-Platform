package com.japaneselearning.module_admin.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_admin.dto.AdminUserRes;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

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
