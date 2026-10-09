package com.japaneselearning.common.security;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for RedisRateLimiterService failure policy behavior.
 * Verifies that fail-open and fail-closed modes work correctly
 * when Redis is unreachable or returns unexpected results.
 */
@ExtendWith(MockitoExtension.class)
class RedisRateLimiterFailurePolicyTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private RedisRateLimiterService service;

    @BeforeEach
    void setUp() {
        service = new RedisRateLimiterService(redisTemplate);
        ReflectionTestUtils.setField(service, "loginMaxIp", 5);
        ReflectionTestUtils.setField(service, "loginMaxEmail", 3);
        ReflectionTestUtils.setField(service, "loginWindow", 15);
        ReflectionTestUtils.setField(service, "refreshMaxIp", 10);
        ReflectionTestUtils.setField(service, "refreshWindow", 15);
    }

    // ==================== Fail-Open Tests ====================

    @Test
    void isAllowed_FailOpen_ShouldReturnTrue_WhenRedisThrowsException() {
        ReflectionTestUtils.setField(service, "redisFailurePolicy", "fail-open");
        when(redisTemplate.execute(any(org.springframework.data.redis.core.SessionCallback.class))).thenThrow(new DataAccessResourceFailureException("Redis connection refused"));

        boolean result = service.isAllowed("rate_limit:login_ip:1.2.3.4", 5, 15);

        assertTrue(result, "Fail-open should allow requests when Redis is down");
    }

    @Test
    void isAllowed_FailOpen_ShouldReturnTrue_WhenRedisReturnsNull() {
        ReflectionTestUtils.setField(service, "redisFailurePolicy", "fail-open");
        when(redisTemplate.execute(any(org.springframework.data.redis.core.SessionCallback.class))).thenReturn(null);

        boolean result = service.isAllowed("rate_limit:login_ip:1.2.3.4", 5, 15);

        assertTrue(result, "Fail-open should allow requests when Redis returns null");
    }

    @Test
    void checkLoginRateLimit_FailOpen_ShouldNotThrow_WhenRedisIsDown() {
        ReflectionTestUtils.setField(service, "redisFailurePolicy", "fail-open");
        when(redisTemplate.execute(any(org.springframework.data.redis.core.SessionCallback.class))).thenThrow(new DataAccessResourceFailureException("Redis unavailable"));

        assertDoesNotThrow(() -> service.checkLoginRateLimit("1.2.3.4", "user@example.com"),
                "Fail-open should not throw exception when Redis is down");
    }

    // ==================== Fail-Closed Tests ====================

    @Test
    void isAllowed_FailClosed_ShouldThrowRateLimitUnavailable_WhenRedisThrowsException() {
        ReflectionTestUtils.setField(service, "redisFailurePolicy", "fail-closed");
        when(redisTemplate.execute(any(org.springframework.data.redis.core.SessionCallback.class))).thenThrow(new DataAccessResourceFailureException("Redis connection refused"));

        AppException exception = assertThrows(AppException.class,
                () -> service.isAllowed("rate_limit:login_ip:1.2.3.4", 5, 15));

        assertEquals(ErrorCode.RATE_LIMIT_UNAVAILABLE, exception.getErrorCode(),
                "Fail-closed should throw RATE_LIMIT_UNAVAILABLE when Redis is down");
    }

    @Test
    void isAllowed_FailClosed_ShouldThrowRateLimitUnavailable_WhenRedisReturnsNull() {
        ReflectionTestUtils.setField(service, "redisFailurePolicy", "fail-closed");
        when(redisTemplate.execute(any(org.springframework.data.redis.core.SessionCallback.class))).thenReturn(null);

        AppException exception = assertThrows(AppException.class,
                () -> service.isAllowed("rate_limit:login_ip:1.2.3.4", 5, 15));

        assertEquals(ErrorCode.RATE_LIMIT_UNAVAILABLE, exception.getErrorCode(),
                "Fail-closed should throw RATE_LIMIT_UNAVAILABLE when Redis returns null");
    }

    @Test
    void checkLoginRateLimit_FailClosed_ShouldThrow_WhenRedisIsDown() {
        ReflectionTestUtils.setField(service, "redisFailurePolicy", "fail-closed");
        when(redisTemplate.execute(any(org.springframework.data.redis.core.SessionCallback.class))).thenThrow(new DataAccessResourceFailureException("Redis unavailable"));

        AppException exception = assertThrows(AppException.class,
                () -> service.checkLoginRateLimit("1.2.3.4", "user@example.com"));

        assertEquals(ErrorCode.RATE_LIMIT_UNAVAILABLE, exception.getErrorCode(),
                "Fail-closed should propagate RATE_LIMIT_UNAVAILABLE through checkLoginRateLimit");
    }

    // ==================== Edge Cases ====================

    @Test
    void isAllowed_ShouldReturnTrue_WhenKeyIsBlank() {
        ReflectionTestUtils.setField(service, "redisFailurePolicy", "fail-closed");

        assertTrue(service.isAllowed("", 5, 15), "Blank key should always be allowed");
        assertTrue(service.isAllowed(null, 5, 15), "Null key should always be allowed");
    }

    @Test
    void isAllowed_ShouldReturnTrue_WhenMaxAttemptsIsZeroOrNegative() {
        ReflectionTestUtils.setField(service, "redisFailurePolicy", "fail-closed");

        assertTrue(service.isAllowed("some_key", 0, 15), "Zero maxAttempts should always be allowed");
        assertTrue(service.isAllowed("some_key", -1, 15), "Negative maxAttempts should always be allowed");
    }
}
