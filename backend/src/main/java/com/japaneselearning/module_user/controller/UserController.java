package com.japaneselearning.module_user.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_user.dto.CurrentUserResponse;
import com.japaneselearning.module_user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Get profile information of the currently authenticated user")
    public ApiResponse<CurrentUserResponse> getCurrentUser() {
        return ApiResponse.success("Get current user successfully", userService.getCurrentUser());
    }
}
