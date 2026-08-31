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
        rateLimiterService = new RateLimiterService();
        ReflectionTestUtils.setField(rateLimiterService, "loginMaxIp", 5);
        ReflectionTestUtils.setField(rateLimiterService, "loginMaxEmail", 3);
        ReflectionTestUtils.setField(rateLimiterService, "loginWindow", 15);
        ReflectionTestUtils.setField(rateLimiterService, "refreshMaxIp", 10);
        ReflectionTestUtils.setField(rateLimiterService, "refreshWindow", 15);
    }

    @Test
    void isAllowed_ShouldAllowRequestsUnderLimit() {
        String key = "test_key";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.isAllowed(key, 5, 1));
        }
    }

    @Test
    void isAllowed_ShouldBlockRequestsOverLimit() {
        String key = "test_key_2";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.isAllowed(key, 5, 1));
        }
        assertFalse(rateLimiterService.isAllowed(key, 5, 1));
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
