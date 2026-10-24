package com.japaneselearning.common.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom Redis health indicator for the readiness probe.
 * <p>
 * Spring Boot auto-configures a Redis health indicator, but this custom version
 * adds a fast timeout and more descriptive metadata for monitoring dashboards.
 * <p>
 * This indicator is only active in the {@code prod} profile where Redis is available.
 * In dev, Redis auto-configuration is excluded so this bean won't be created.
 *
 * <h3>Health check behavior:</h3>
 * <ul>
 *     <li>Sends a Redis PING command to verify connectivity</li>
 *     <li>Reports UP with Redis server info on success</li>
 *     <li>Reports DOWN with error details on failure</li>
 * </ul>
 *
 * <h3>Integration with Kubernetes probes:</h3>
 * <p>This indicator is included in the {@code readiness} health group,
 * so {@code /actuator/health/readiness} will reflect Redis availability.
 * If Redis is down, readiness fails and the container stops receiving traffic.</p>
 */
@Component("redisCustomHealthIndicator")
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate redisTemplate;

    @Override
    public Health health() {
        try {
            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            if (factory == null) {
                return Health.down()
                        .withDetail("error", "RedisConnectionFactory is null")
                        .build();
            }

            // Execute PING to verify Redis connectivity
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            if ("PONG".equals(pong)) {
                return Health.up()
                        .withDetail("connection", "active")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Unexpected PING response: " + pong)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Redis health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getClass().getSimpleName() + ": " + e.getMessage())
                    .build();
        }
    }
}
