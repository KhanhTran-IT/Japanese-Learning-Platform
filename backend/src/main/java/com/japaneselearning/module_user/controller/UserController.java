package com.japaneselearning.module_user.controller;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_leaderboard.dto.LearningActivityRes;
import com.japaneselearning.module_leaderboard.service.LearningActivityService;
import com.japaneselearning.module_user.dto.ChangePasswordReq;
import com.japaneselearning.module_user.dto.CurrentUserResponse;
import com.japaneselearning.module_user.dto.UpdateCurrentUserReq;
import com.japaneselearning.module_user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    private final LearningActivityService learningActivityService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Get profile information of the currently authenticated user")
    public ApiResponse<CurrentUserResponse> getCurrentUser() {
        return ApiResponse.success("Get current user successfully", userService.getCurrentUser());
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update profile and leaderboard privacy settings of the currently authenticated user")
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

    @GetMapping({"/me/activities", "/api/v1/users/me/activities"})
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my learning activities", description = "Returns paginated activity history of the current student, sorted deterministically by createdAt DESC, id DESC")
    public ApiResponse<PageResponse<LearningActivityRes>> getMyActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be less than one");
        }
        if (size > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be greater than 100");
        }
        return ApiResponse.success("Lấy lịch sử hoạt động thành công",
                learningActivityService.getMyActivities(PageRequest.of(page, size)));
    }
}
