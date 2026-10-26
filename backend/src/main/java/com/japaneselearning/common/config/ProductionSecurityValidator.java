package com.japaneselearning.common.config;

import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Fail-fast startup validator for production environment.
 * Validates that all required secrets, database credentials, Redis configuration,
 * JWT keys, and CORS settings are properly configured and do not use insecure defaults
 * or placeholder values.
 *
 * Active ONLY under the "prod" profile to keep development and test configurations
 * working separately.
 */
@Component
@Profile("prod")
@Slf4j
@Getter
@Setter
public class ProductionSecurityValidator {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            "(?i).*(CHANGE_ME|YOUR_STRONG_|SECRET_HERE|PASSWORD_HERE|PLACEHOLDER|GENERATE_WITH|DEFAULT_PASSWORD).*");

    private static final Set<String> FORBIDDEN_WEAK_PASSWORDS = Set.of(
            "password", "admin", "root", "123456", "password@123", "admin_password", "db_password", "redis_password"
    );

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.cors.allowed-origins:}")
    private List<String> corsAllowedOrigins;

    @Value("${jwt.secret.access:}")
    private String jwtAccessSecret;

    @Value("${jwt.secret.refresh:}")
    private String jwtRefreshSecret;

    @Value("${spring.data.redis.host:}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort = 6379;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure = true;

    @Value("${app.cookie.same-site:Strict}")
    private String cookieSameSite = "Strict";

    @Value("${app.rate-limit.redis-failure-policy:FAIL_CLOSED}")
    private String redisFailurePolicy = "FAIL_CLOSED";

    @Value("${app.payment.simulation-enabled:false}")
    private boolean paymentSimulationEnabled = false;

    @PostConstruct
    public void validateOnStartup() {
        log.info("Starting fail-fast production security configuration validation...");
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            String errorMessage = String.format(
                    "Production configuration validation failed with %d violation(s):%n%s%n" +
                    "Aborting startup. Please supply valid production configuration via environment variables.",
                    errors.size(),
                    errors.stream().map(e -> "  - " + e).collect(Collectors.joining(System.lineSeparator()))
            );
            log.error("PRODUCTION CONFIGURATION ERROR:%n{}", errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        log.info("Production security configuration validation PASSED. All required secrets and settings are verified.");
    }

    /**
     * Executes all production validation rules and returns a list of error descriptions.
     * Can be invoked directly by tests.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        validateJwtSecrets(errors);
        validateAdminPassword(errors);
        validateDatabase(errors);
        validateRedis(errors);
        validateCors(errors);
        validateCookies(errors);
        validateRateLimitPolicy(errors);
        validatePaymentSimulation(errors);
        return errors;
    }

    private void validateJwtSecrets(List<String> errors) {
        // Access Secret
        if (datasourceUrl == null) {
            // Null-safe guard
        }
        if (jwtAccessSecret == null || jwtAccessSecret.isBlank()) {
            errors.add("[JWT] 'jwt.secret.access' must be configured.");
        } else if (isPlaceholder(jwtAccessSecret)) {
            errors.add("[JWT] 'jwt.secret.access' contains forbidden placeholder: '" + maskSecret(jwtAccessSecret) + "'.");
        } else {
            try {
                byte[] bytes = Decoders.BASE64.decode(jwtAccessSecret.trim());
                if (bytes.length < 32) {
                    errors.add("[JWT] 'jwt.secret.access' must decode to at least 256 bits (32 bytes), but was " + (bytes.length * 8) + " bits.");
                }
            } catch (Exception e) {
                errors.add("[JWT] 'jwt.secret.access' is not a valid Base64-encoded string: " + e.getMessage());
            }
        }

        // Refresh Secret
        if (jwtRefreshSecret == null || jwtRefreshSecret.isBlank()) {
            errors.add("[JWT] 'jwt.secret.refresh' must be configured.");
        } else if (isPlaceholder(jwtRefreshSecret)) {
            errors.add("[JWT] 'jwt.secret.refresh' contains forbidden placeholder: '" + maskSecret(jwtRefreshSecret) + "'.");
        } else {
            try {
                byte[] bytes = Decoders.BASE64.decode(jwtRefreshSecret.trim());
                if (bytes.length < 32) {
                    errors.add("[JWT] 'jwt.secret.refresh' must decode to at least 256 bits (32 bytes), but was " + (bytes.length * 8) + " bits.");
                }
            } catch (Exception e) {
                errors.add("[JWT] 'jwt.secret.refresh' is not a valid Base64-encoded string: " + e.getMessage());
            }
        }

        // Distinctness check
        if (jwtAccessSecret != null && jwtRefreshSecret != null && !jwtAccessSecret.isBlank() && !jwtRefreshSecret.isBlank()) {
            if (jwtAccessSecret.trim().equals(jwtRefreshSecret.trim())) {
                errors.add("[JWT] 'jwt.secret.access' and 'jwt.secret.refresh' MUST NOT be identical. Separate secrets are required for access and refresh tokens.");
            }
        }
    }

    private void validateAdminPassword(List<String> errors) {
        if (adminPassword == null || adminPassword.isBlank()) {
            errors.add("[Admin] 'app.admin.password' must be configured.");
        } else if (isPlaceholder(adminPassword) || FORBIDDEN_WEAK_PASSWORDS.contains(adminPassword.trim().toLowerCase())) {
            errors.add("[Admin] 'app.admin.password' cannot use a placeholder or common default password.");
        } else if (adminPassword.trim().length() < 12) {
            errors.add("[Admin] 'app.admin.password' must be at least 12 characters in production.");
        }
    }

    private void validateDatabase(List<String> errors) {
        if (datasourceUrl == null || datasourceUrl.isBlank()) {
            errors.add("[Database] 'spring.datasource.url' must be configured.");
        } else if (isPlaceholder(datasourceUrl)) {
            errors.add("[Database] 'spring.datasource.url' contains a placeholder value.");
        } else {
            String urlLower = datasourceUrl.toLowerCase();
            if (urlLower.contains("jdbc:h2:") || urlLower.contains("jdbc:hsqldb:")) {
                errors.add("[Database] In-memory database URL '" + datasourceUrl + "' is strictly prohibited in production.");
            }
        }

        if (datasourceUsername == null || datasourceUsername.isBlank()) {
            errors.add("[Database] 'spring.datasource.username' must be configured.");
        } else if (isPlaceholder(datasourceUsername)) {
            errors.add("[Database] 'spring.datasource.username' contains a placeholder value.");
        } else if ("root".equalsIgnoreCase(datasourceUsername.trim())) {
            errors.add("[Database] 'spring.datasource.username' must not be 'root' in production. Use a dedicated least-privilege application user.");
        }

        if (datasourcePassword == null || datasourcePassword.isBlank()) {
            errors.add("[Database] 'spring.datasource.password' must be configured.");
        } else if (isPlaceholder(datasourcePassword) || FORBIDDEN_WEAK_PASSWORDS.contains(datasourcePassword.trim().toLowerCase())) {
            errors.add("[Database] 'spring.datasource.password' cannot use a placeholder or common weak password.");
        } else if (datasourcePassword.trim().length() < 8) {
            errors.add("[Database] 'spring.datasource.password' must be at least 8 characters in production.");
        }
    }

    private void validateRedis(List<String> errors) {
        if (redisHost == null || redisHost.isBlank()) {
            errors.add("[Redis] 'spring.data.redis.host' must be configured in production.");
        } else if (isPlaceholder(redisHost)) {
            errors.add("[Redis] 'spring.data.redis.host' contains a placeholder value.");
        } else if ("localhost".equalsIgnoreCase(redisHost.trim()) || "127.0.0.1".equals(redisHost.trim())) {
            errors.add("[Redis] 'spring.data.redis.host' must not point to localhost in production. Use a dedicated container name or external service.");
        }

        if (redisPort < 1 || redisPort > 65535) {
            errors.add("[Redis] 'spring.data.redis.port' must be between 1 and 65535 (was " + redisPort + ").");
        }

        if (redisPassword == null || redisPassword.isBlank()) {
            errors.add("[Redis] 'spring.data.redis.password' must be configured in production (authentication is required).");
        } else if (isPlaceholder(redisPassword) || FORBIDDEN_WEAK_PASSWORDS.contains(redisPassword.trim().toLowerCase())) {
            errors.add("[Redis] 'spring.data.redis.password' cannot use a placeholder or weak password.");
        } else if (redisPassword.trim().length() < 8) {
            errors.add("[Redis] 'spring.data.redis.password' must be at least 8 characters in production.");
        }
    }

    private void validateCors(List<String> errors) {
        if (corsAllowedOrigins == null || corsAllowedOrigins.isEmpty()) {
            errors.add("[CORS] 'app.cors.allowed-origins' must be explicitly configured in production.");
            return;
        }

        List<String> trimmedOrigins = corsAllowedOrigins.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (trimmedOrigins.isEmpty()) {
            errors.add("[CORS] 'app.cors.allowed-origins' contains no non-empty origins.");
            return;
        }

        for (String origin : trimmedOrigins) {
            if ("*".equals(origin)) {
                errors.add("[CORS] Wildcard origin '*' is strictly prohibited in production.");
            } else if (origin.toLowerCase().contains("localhost") || origin.contains("127.0.0.1")) {
                errors.add("[CORS] Unsafe localhost origin '" + origin + "' is not permitted in production.");
            } else if (!origin.startsWith("https://")) {
                errors.add("[CORS] Origin '" + origin + "' must use the HTTPS scheme in production.");
            } else if (origin.endsWith("/")) {
                errors.add("[CORS] Origin '" + origin + "' must not contain a trailing slash.");
            } else {
                try {
                    URI uri = URI.create(origin);
                    if (uri.getHost() == null) {
                        errors.add("[CORS] Origin '" + origin + "' does not contain a valid host.");
                    }
                } catch (Exception e) {
                    errors.add("[CORS] Origin '" + origin + "' is not a valid URI: " + e.getMessage());
                }
            }
        }
    }

    private void validateCookies(List<String> errors) {
        if (!cookieSecure) {
            errors.add("[Cookie] 'app.cookie.secure' MUST be true in production to prevent cookie transmission over plain HTTP.");
        }

        if (cookieSameSite == null || (!cookieSameSite.equalsIgnoreCase("Strict") &&
                !cookieSameSite.equalsIgnoreCase("Lax") &&
                !cookieSameSite.equalsIgnoreCase("None"))) {
            errors.add("[Cookie] 'app.cookie.same-site' must be one of 'Strict', 'Lax', or 'None' (was '" + cookieSameSite + "').");
        } else if ("None".equalsIgnoreCase(cookieSameSite) && !cookieSecure) {
            errors.add("[Cookie] 'app.cookie.same-site=None' requires 'app.cookie.secure=true'.");
        }
    }

    private void validateRateLimitPolicy(List<String> errors) {
        if (redisFailurePolicy == null || (!redisFailurePolicy.equalsIgnoreCase("FAIL_CLOSED") &&
                !redisFailurePolicy.equalsIgnoreCase("FAIL_OPEN"))) {
            errors.add("[RateLimiter] 'app.rate-limit.redis-failure-policy' must be either FAIL_CLOSED or FAIL_OPEN (was '" + redisFailurePolicy + "').");
        }
    }

    private void validatePaymentSimulation(List<String> errors) {
        if (paymentSimulationEnabled) {
            errors.add("[Payment] 'app.payment.simulation-enabled' MUST NOT be enabled (true) in production.");
        }
    }

    private boolean isPlaceholder(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        return PLACEHOLDER_PATTERN.matcher(trimmed).matches();
    }

    private String maskSecret(String secret) {
        if (secret == null) return "null";
        if (secret.length() <= 8) return "***";
        return secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4);
    }
}
