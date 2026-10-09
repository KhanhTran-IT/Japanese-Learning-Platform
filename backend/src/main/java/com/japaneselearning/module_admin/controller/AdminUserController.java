package com.japaneselearning.module_admin.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_admin.dto.AdminUserRes;
import com.japaneselearning.module_admin.service.AdminUserService;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResponse<AdminUserRes>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) RoleName role) {
        
        if (page < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be less than one");
        }
        if (size > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be greater than 100");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<AdminUserRes> result = adminUserService.getUsers(keyword, status, role, pageable);
        
        return ApiResponse.success("Lấy danh sách người dùng thành công", result);
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserRes> getUserDetail(@PathVariable Long id) {
        return ApiResponse.success("Lấy chi tiết người dùng thành công", adminUserService.getUserDetail(id));
    }

    @PutMapping("/{id}/lock")
    public ApiResponse<AdminUserRes> lockUser(@PathVariable Long id) {
        return ApiResponse.success("Khóa tài khoản thành công", adminUserService.lockUser(id));
    }

    @PutMapping("/{id}/unlock")
    public ApiResponse<AdminUserRes> unlockUser(@PathVariable Long id) {
        return ApiResponse.success("Mở khóa tài khoản thành công", adminUserService.unlockUser(id));
    }
}
