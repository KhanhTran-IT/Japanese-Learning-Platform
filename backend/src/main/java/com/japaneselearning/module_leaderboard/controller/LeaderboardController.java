package com.japaneselearning.module_leaderboard.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_leaderboard.dto.LeaderboardRes;
import com.japaneselearning.module_leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get leaderboard ranking", description = "Get top users based on timeframe (WEEKLY, MONTHLY, ALL_TIME).")
    public ApiResponse<LeaderboardRes> getLeaderboard(
            @RequestParam(defaultValue = "ALL_TIME") String timeframe,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success("Lấy bảng xếp hạng thành công", leaderboardService.getLeaderboard(timeframe, page, size));
    }
}
