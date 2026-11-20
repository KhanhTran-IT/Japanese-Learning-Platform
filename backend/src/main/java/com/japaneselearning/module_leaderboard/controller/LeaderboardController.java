package com.japaneselearning.module_leaderboard.controller;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_leaderboard.dto.LeaderboardRes;
import com.japaneselearning.module_leaderboard.dto.LearningActivityRes;
import com.japaneselearning.module_leaderboard.service.LeaderboardService;
import com.japaneselearning.module_leaderboard.service.LearningActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaderboards")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "APIs for leaderboard and rankings")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final LearningActivityService learningActivityService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get leaderboard ranking", description = "Get top users based on timeframe (WEEKLY, MONTHLY, ALL_TIME) with safe pagination limits.")
    public ApiResponse<LeaderboardRes> getLeaderboard(
            @RequestParam(defaultValue = "ALL_TIME") String timeframe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (page < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be less than one");
        }
        if (size > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be greater than 100");
        }
        return ApiResponse.success("Lấy bảng xếp hạng thành công", leaderboardService.getLeaderboard(timeframe, page, size));
    }

    @GetMapping({"/activities", "/me/activities"})
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
