package com.japaneselearning.common.security;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory rate limiter to protect authentication endpoints.
 * Uses a sliding window mechanism.
 * 
 * LIMITATIONS:
 * - Stores state in-memory, which can lead to memory leaks if not evicted.
 * - Does not sync across multiple instances.
 * - For production with horizontal scaling, replace with a Redis-backed implementation.
 */
@Service
public class InMemoryRateLimiterService implements RateLimiterService {

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

    @Override
    public void checkLoginRateLimit(String ip, String email) {
        if (!isAllowed("login_ip:" + ip, loginMaxIp, loginWindow)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
        if (email != null) {
            String normalizedEmail = email.trim().toLowerCase();
            if (!isAllowed("login_email:" + normalizedEmail, loginMaxEmail, loginWindow)) {
                throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
            }
        }
    }

    @Override
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

    /**
     * Evicts stale keys to prevent memory leak (OOM).
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000)
    public void evictStaleKeys() {
        int maxWindowMinutes = Math.max(loginWindow, refreshWindow);
        Instant windowStart = Instant.now().minusSeconds(maxWindowMinutes * 60L);

        attemptsCache.entrySet().removeIf(entry -> {
            Deque<Instant> attempts = entry.getValue();
            synchronized (attempts) {
                while (!attempts.isEmpty() && attempts.peekFirst().isBefore(windowStart)) {
                    attempts.pollFirst();
                }
                return attempts.isEmpty();
            }
        });
    }
}
