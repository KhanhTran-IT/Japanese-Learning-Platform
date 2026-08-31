package com.japaneselearning.common.security;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory rate limiter to protect authentication endpoints.
 * Uses a sliding window mechanism.
 */
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, Deque<Instant>> attemptsCache = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.login.max-attempts-per-ip:20}")
    private int loginMaxIp;

    @Value("${app.rate-limit.login.max-attempts-per-email:10}")
    private int loginMaxEmail;

    @Value("${app.rate-limit.login.window-minutes:15}")
    private int loginWindow;

    @Value("${app.rate-limit.refresh.max-attempts-per-ip:30}")
    private int refreshMaxIp;

    @Value("${app.rate-limit.refresh.window-minutes:15}")
    private int refreshWindow;

    public void checkLoginRateLimit(String ip, String email) {
        if (!isAllowed("login_ip:" + ip, loginMaxIp, loginWindow)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
        if (email != null && !isAllowed("login_email:" + email, loginMaxEmail, loginWindow)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    public void checkRefreshRateLimit(String ip) {
        if (!isAllowed("refresh_ip:" + ip, refreshMaxIp, refreshWindow)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    /**
     * Checks if a request is allowed based on the sliding window.
     *
     * @param key          The unique identifier.
     * @param maxAttempts  Maximum number of attempts allowed in the window.
     * @param windowMinutes The time window in minutes.
     * @return true if allowed, false if rate limited.
     */
    public boolean isAllowed(String key, int maxAttempts, int windowMinutes) {
        if (key == null || key.isBlank() || maxAttempts <= 0) {
            return true;
        }

        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(windowMinutes * 60L);

        Deque<Instant> attempts = attemptsCache.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(windowStart)) {
                attempts.pollFirst();
            }

            if (attempts.size() >= maxAttempts) {
                return false;
            }

            attempts.addLast(now);
            return true;
        }
    }
}
