package com.japaneselearning.module_user.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_user.dto.ChangePasswordReq;
import com.japaneselearning.module_user.dto.CurrentUserResponse;
import com.japaneselearning.module_user.dto.UpdateCurrentUserReq;
import com.japaneselearning.module_user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.japaneselearning.module_user.service.StudentDashboardService;
import java.util.List;
import com.japaneselearning.module_learning.dto.MyCourseRes;
import org.springframework.security.access.prepost.PreAuthorize;
import com.japaneselearning.module_learning.dto.MyProgressOverviewRes;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User APIs")
public class UserController {

    private final UserService userService;
    private final StudentDashboardService studentDashboardService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Get profile information of the currently authenticated user")
    public ApiResponse<CurrentUserResponse> getCurrentUser() {
        return ApiResponse.success("Get current user successfully", userService.getCurrentUser());
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update fullName, phone, avatarUrl of the currently authenticated user")
    public ApiResponse<CurrentUserResponse> updateCurrentUser(@Valid @RequestBody UpdateCurrentUserReq request) {
        return ApiResponse.success("Cập nhật thông tin thành công", userService.updateCurrentUser(request));
    }

    @PutMapping("/me/change-password")
    @Operation(summary = "Change password", description = "Change password of the currently authenticated user")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordReq request) {
        userService.changePassword(request);
        return ApiResponse.success("Đổi mật khẩu thành công", null);
    }

    @GetMapping("/me/courses")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my enrolled courses", description = "Returns a list of courses the current student has enrolled in along with progress.")
    public ApiResponse<List<MyCourseRes>> getMyCourses() {
        return ApiResponse.success("Lấy danh sách khóa học thành công", studentDashboardService.getMyCourses());
    }

    @GetMapping("/me/progress")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my learning progress overview", description = "Returns overall progress statistics for the current student.")
    public ApiResponse<MyProgressOverviewRes> getMyProgressOverview() {
        return ApiResponse.success("Lấy tổng quan tiến độ thành công", studentDashboardService.getMyProgressOverview());
    }
}
