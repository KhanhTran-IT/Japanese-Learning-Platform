package com.japaneselearning.module_admin.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_admin.dto.AdminDashboardRes;
import com.japaneselearning.module_admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ApiResponse<AdminDashboardRes> getDashboardStats() {
        return ApiResponse.success("Lấy dữ liệu dashboard thành công", adminDashboardService.getDashboardStats());
    }
}
