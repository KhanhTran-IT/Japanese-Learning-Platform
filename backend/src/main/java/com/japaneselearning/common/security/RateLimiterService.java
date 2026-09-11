package com.japaneselearning.common.security;

/**
 * Interface for rate limiting authentication endpoints.
 * Provides a clean path for Redis/shared rate limiting in horizontally scaled deployments.
 * Implementations should handle limits on login and refresh operations.
 */
public interface RateLimiterService {
    void checkLoginRateLimit(String ip, String email);
    void checkRefreshRateLimit(String ip);
}
