package com.japaneselearning.module_user.controller;

import com.japaneselearning.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin Test APIs")
public class AdminTestController {

    @GetMapping("/test")
    @Operation(summary = "Test Admin Role", description = "Endpoint to test if current user has ADMIN role")
    public ApiResponse<String> testAdminRole() {
        return ApiResponse.success("Hello Admin! You have sufficient permissions to access this endpoint.", null);
    }
}
