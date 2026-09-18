package com.japaneselearning.common.security;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed rate limiter for production deployments.
 * Uses Redis Sorted Sets to implement a sliding window algorithm.
 *
 * How it works:
 * - Each rate-limit key maps to a Redis Sorted Set where members are unique
 *   request identifiers (timestamps + nanos) and scores are epoch-second timestamps.
 * - On each request:
 *   1. ZREMRANGEBYSCORE removes entries older than the window.
 *   2. ZCARD counts remaining entries.
 *   3. If under limit, ZADD adds the new entry.
 * - Steps 1-3 run in a Redis MULTI/EXEC transaction for atomicity.
 * - Each key has a TTL equal to the window duration, so Redis auto-evicts stale keys.
 *
 * This implementation shares state across all backend instances via Redis,
 * making it suitable for horizontally scaled deployments.
 */
@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiterService implements RateLimiterService {

    private final StringRedisTemplate redisTemplate;

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

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    public void checkLoginRateLimit(String ip, String email) {
        if (!isAllowed(RATE_LIMIT_PREFIX + "login_ip:" + ip, loginMaxIp, loginWindow)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
        if (email != null) {
            String normalizedEmail = email.trim().toLowerCase();
            if (!isAllowed(RATE_LIMIT_PREFIX + "login_email:" + normalizedEmail, loginMaxEmail, loginWindow)) {
                throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
            }
        }
    }

    @Override
    public void checkRefreshRateLimit(String ip) {
        if (!isAllowed(RATE_LIMIT_PREFIX + "refresh_ip:" + ip, refreshMaxIp, refreshWindow)) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    /**
     * Checks if a request is allowed using Redis sorted sets (sliding window).
     *
     * @param key          The Redis key for the rate limit bucket.
     * @param maxAttempts  Maximum number of attempts allowed in the window.
     * @param windowMinutes The time window in minutes.
     * @return true if allowed, false if rate limited.
     */
    boolean isAllowed(String key, int maxAttempts, int windowMinutes) {
        if (key == null || key.isBlank() || maxAttempts <= 0) {
            return true;
        }

        try {
            Instant now = Instant.now();
            double windowStart = now.minusSeconds(windowMinutes * 60L).toEpochMilli();
            double nowScore = now.toEpochMilli();
            // Unique member to avoid dedup in the sorted set
            String member = now.toEpochMilli() + ":" + now.getNano() + ":" + Thread.currentThread().threadId();

            List<Object> results = redisTemplate.execute(new SessionCallback<>() {
                @Override
                @SuppressWarnings("unchecked")
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    // 1. Remove entries outside the window
                    operations.opsForZSet().removeRangeByScore(key, 0, windowStart);
                    // 2. Count remaining entries
                    operations.opsForZSet().zCard(key);
                    // 3. Add this attempt
                    operations.opsForZSet().add(key, member, nowScore);
                    // 4. Set TTL to auto-expire the key after the window passes
                    operations.expire(key, windowMinutes * 60L + 60, TimeUnit.SECONDS);
                    return operations.exec();
                }
            });

            if (results == null || results.size() < 2) {
                log.warn("Redis rate limit transaction returned unexpected results for key: {}", key);
                return true; // Fail open on unexpected Redis behavior
            }

            // results[1] = ZCARD result (count before the new entry was added)
            Long currentCount = (Long) results.get(1);
            if (currentCount != null && currentCount >= maxAttempts) {
                // Over limit — remove the entry we just added
                redisTemplate.opsForZSet().remove(key, member);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Redis rate limiter error for key: {}. Failing open.", key, e);
            // Fail open: if Redis is down, don't block legitimate users
            return true;
        }
    }
}
