package com.japaneselearning.common.security;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        InMemoryRateLimiterService service = new InMemoryRateLimiterService();
        ReflectionTestUtils.setField(service, "loginMaxIp", 5);
        ReflectionTestUtils.setField(service, "loginMaxEmail", 3);
        ReflectionTestUtils.setField(service, "loginWindow", 15);
        ReflectionTestUtils.setField(service, "refreshMaxIp", 10);
        ReflectionTestUtils.setField(service, "refreshWindow", 15);
        rateLimiterService = service;
    }

    @Test
    void checkLoginRateLimit_ShouldAllowRequestsUnderLimit() {
        String ip = "192.168.1.1";
        String email = "test@example.com";
        for (int i = 0; i < 3; i++) {
            rateLimiterService.checkLoginRateLimit(ip, email);
        }
    }

    @Test
    void checkLoginRateLimit_ShouldThrowWhenIpLimitExceeded() {
        String ip = "192.168.1.1";
        String email = "test@example.com";

        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginRateLimit(ip, email + i); // Different emails to bypass email limit
        }

        AppException exception = assertThrows(AppException.class, () -> 
            rateLimiterService.checkLoginRateLimit(ip, "another@example.com"));
        
        assertTrue(exception.getErrorCode() == ErrorCode.TOO_MANY_REQUESTS);
    }

    @Test
    void checkLoginRateLimit_ShouldThrowWhenEmailLimitExceeded() {
        String email = "target@example.com";

        for (int i = 0; i < 3; i++) {
            rateLimiterService.checkLoginRateLimit("192.168.1." + i, email); // Different IPs to bypass IP limit
        }

        AppException exception = assertThrows(AppException.class, () -> 
            rateLimiterService.checkLoginRateLimit("192.168.1.99", email));
        
        assertTrue(exception.getErrorCode() == ErrorCode.TOO_MANY_REQUESTS);
    }

    @Test
    void checkRefreshRateLimit_ShouldAllowRequestsUnderLimit() {
        String ip = "10.0.0.1";
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkRefreshRateLimit(ip);
        }
    }

    @Test
    void checkRefreshRateLimit_ShouldThrowWhenLimitExceeded() {
        String ip = "10.0.0.1";

        for (int i = 0; i < 10; i++) {
            rateLimiterService.checkRefreshRateLimit(ip);
        }

        AppException exception = assertThrows(AppException.class, () -> 
            rateLimiterService.checkRefreshRateLimit(ip));
        
        assertTrue(exception.getErrorCode() == ErrorCode.TOO_MANY_REQUESTS);
    }
}
