package com.japaneselearning.module_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.service.AuthService;
import com.japaneselearning.module_auth.util.CookieUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "app.rate-limit.login.max-attempts-per-ip=5",
    "app.rate-limit.login.max-attempts-per-email=3"
})
@AutoConfigureMockMvc
public class AuthRateLimitIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CookieUtil cookieUtil;

    @Test
    void login_ExceedsEmailRateLimit_Returns429() throws Exception {
        LoginRequest request = new LoginRequest("throttled@example.com", "password");
        
        // Mock to throw 401 just so it doesn't fail on missing mock response, 
        // we only care about 429 when rate limit is hit.
        org.mockito.Mockito.doThrow(new com.japaneselearning.common.exception.AppException(ErrorCode.LOGIN_FAILED)).when(authService).login(any());

        // 1st to 3rd attempt from different IPs should hit 401 (service level)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .header("X-Forwarded-For", "10.0.0." + i)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        // 4th attempt should hit 429 (Too Many Requests) from RateLimiterService
        mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", "10.0.0.99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(ErrorCode.TOO_MANY_REQUESTS.getCode()));
    }

    @Test
    void login_ExceedsIpRateLimit_Returns429() throws Exception {
        // 1st to 5th attempt from the same IP with different emails
        for (int i = 0; i < 5; i++) {
            LoginRequest request = new LoginRequest("user" + i + "@example.com", "password");
            org.mockito.Mockito.doThrow(new com.japaneselearning.common.exception.AppException(ErrorCode.LOGIN_FAILED)).when(authService).login(any());
            
            mockMvc.perform(post("/api/auth/login")
                    .header("X-Forwarded-For", "192.168.1.100")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        // 6th attempt from the same IP should hit 429
        LoginRequest request = new LoginRequest("newuser@example.com", "password");
        mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", "192.168.1.100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(ErrorCode.TOO_MANY_REQUESTS.getCode()));
    }
}
