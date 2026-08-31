package com.japaneselearning.module_auth.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.LoginResponse;
import com.japaneselearning.module_auth.dto.LogoutRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenRequest;
import com.japaneselearning.module_auth.dto.RefreshTokenResponse;
import com.japaneselearning.module_auth.dto.RegisterRequest;
import com.japaneselearning.module_auth.dto.RegisterResponse;
import com.japaneselearning.module_auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.http.HttpHeaders;
import com.japaneselearning.module_auth.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.japaneselearning.common.security.RateLimiterService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final RateLimiterService rateLimiterService;

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new student account with default STUDENT role")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ApiResponse.success("Registration successful", response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT access token, sets HttpOnly refresh token cookie")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        rateLimiterService.checkLoginRateLimit(getClientIp(httpRequest), request.getEmail());
        
        LoginResponse response = authService.login(request);
        
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, 
                cookieUtil.createRefreshTokenCookie(response.getRefreshToken()).toString());
        
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get a new access token using a valid HttpOnly refresh token cookie")
    public ApiResponse<RefreshTokenResponse> refreshToken(
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        rateLimiterService.checkRefreshRateLimit(getClientIp(httpRequest));

        if (refreshToken == null || refreshToken.isBlank()) {
            return ApiResponse.error(401, "Missing refresh token cookie");
        }
        
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        RefreshTokenResponse response = authService.refreshToken(request);
        
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, 
                cookieUtil.createRefreshTokenCookie(response.getRefreshToken()).toString());
                
        return ApiResponse.success("Refresh token successfully", response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revoke the current refresh token and clear the cookie")
    public ApiResponse<Void> logout(
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse httpResponse) {
        
        if (refreshToken != null && !refreshToken.isBlank()) {
            LogoutRequest request = new LogoutRequest(refreshToken);
            authService.logout(request);
        }
        
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, 
                cookieUtil.createClearRefreshTokenCookie().toString());
                
        return ApiResponse.success("Logout successfully", null);
    }
}
