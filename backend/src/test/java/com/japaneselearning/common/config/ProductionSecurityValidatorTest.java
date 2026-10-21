package com.japaneselearning.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductionSecurityValidator Unit Tests")
class ProductionSecurityValidatorTest {

    private ProductionSecurityValidator validator;

    // A valid 256-bit (32-byte) Base64 secret
    private static final String VALID_ACCESS_SECRET = "NDBlNjM1MjY2NTVhNjg2ZTMyNzI1NzU4NzgvNDEzRjQ0Mjg0NzIrNEI2MlA2ZFM2N1Y2azlw";
    // Another distinct valid 256-bit Base64 secret
    private static final String VALID_REFRESH_SECRET = "NTA2NDUzNjc1NmtiNTlwMzM3czM2NzYzOTc5MjQ0MjI2NDUyOTQ4NDA0RDYzNTE2NmU0NmE1";

    @BeforeEach
    void setUp() {
        validator = new ProductionSecurityValidator();
        // Set standard valid production values
        validator.setDatasourceUrl("jdbc:mariadb://db.production.internal:3306/japanese_learning_db?useSSL=true");
        validator.setDatasourceUsername("app_prod_user");
        validator.setDatasourcePassword("SuperSecureDbP@ssw0rd2026!");
        validator.setAdminPassword("ProductionAdminP@ssw0rd2026!#");
        validator.setCorsAllowedOrigins(List.of("https://japaneselearning.com", "https://admin.japaneselearning.com"));
        validator.setJwtAccessSecret(VALID_ACCESS_SECRET);
        validator.setJwtRefreshSecret(VALID_REFRESH_SECRET);
        validator.setRedisHost("redis.production.internal");
        validator.setRedisPort(6379);
        validator.setRedisPassword("SuperSecureRedisP@ss2026!");
        validator.setCookieSecure(true);
        validator.setCookieSameSite("Strict");
        validator.setRedisFailurePolicy("FAIL_CLOSED");
        validator.setPaymentSimulationEnabled(false);
    }

    @Test
    @DisplayName("Should pass when all production configurations are valid and secure")
    void shouldPassWithValidProductionConfiguration() {
        List<String> errors = validator.validate();
        assertTrue(errors.isEmpty(), () -> "Expected no errors, but found: " + errors);
        assertDoesNotThrow(() -> validator.validateOnStartup());
    }

    @Nested
    @DisplayName("JWT Secret Validation")
    class JwtSecretValidationTests {

        @Test
        @DisplayName("Should reject placeholder in access secret")
        void shouldRejectPlaceholderInAccessSecret() {
            validator.setJwtAccessSecret("CHANGE_ME_GENERATE_WITH_OPENSSL_RAND_BASE64_32");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[JWT]") && e.contains("placeholder")));
        }

        @Test
        @DisplayName("Should reject placeholder in refresh secret")
        void shouldRejectPlaceholderInRefreshSecret() {
            validator.setJwtRefreshSecret("YOUR_STRONG_SECRET_HERE_123456789012345");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[JWT]") && e.contains("placeholder")));
        }

        @Test
        @DisplayName("Should reject short JWT secret (< 256 bits / 32 bytes)")
        void shouldRejectShortJwtSecret() {
            // "c2VjcmV0" is Base64 for "secret" (6 bytes = 48 bits)
            validator.setJwtAccessSecret("c2VjcmV0");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[JWT]") && e.contains("at least 256 bits")));
        }

        @Test
        @DisplayName("Should reject invalid Base64 string for JWT secret")
        void shouldRejectInvalidBase64Secret() {
            validator.setJwtAccessSecret("Not a valid Base64 string with spaces!!!");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[JWT]") && e.contains("Base64")));
        }

        @Test
        @DisplayName("Should reject missing or blank JWT secrets")
        void shouldRejectMissingJwtSecrets() {
            validator.setJwtAccessSecret("");
            validator.setJwtRefreshSecret(null);
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[JWT] 'jwt.secret.access' must be configured")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("[JWT] 'jwt.secret.refresh' must be configured")));
        }

        @Test
        @DisplayName("Should reject identical access and refresh secrets")
        void shouldRejectIdenticalSecrets() {
            validator.setJwtAccessSecret(VALID_ACCESS_SECRET);
            validator.setJwtRefreshSecret(VALID_ACCESS_SECRET);
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[JWT]") && e.contains("MUST NOT be identical")));
        }
    }

    @Nested
    @DisplayName("Admin Password Validation")
    class AdminPasswordValidationTests {

        @Test
        @DisplayName("Should reject missing admin password")
        void shouldRejectMissingAdminPassword() {
            validator.setAdminPassword(null);
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Admin]") && e.contains("must be configured")));
        }

        @Test
        @DisplayName("Should reject placeholder admin password")
        void shouldRejectPlaceholderAdminPassword() {
            validator.setAdminPassword("YOUR_STRONG_ADMIN_PASSWORD_HERE");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Admin]") && e.contains("placeholder")));
        }

        @Test
        @DisplayName("Should reject common default admin password")
        void shouldRejectCommonDefaultAdminPassword() {
            validator.setAdminPassword("Password@123");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Admin]") && e.contains("common default")));
        }

        @Test
        @DisplayName("Should reject short admin password (< 12 chars)")
        void shouldRejectShortAdminPassword() {
            validator.setAdminPassword("Short1!");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Admin]") && e.contains("at least 12 characters")));
        }
    }

    @Nested
    @DisplayName("Database Validation")
    class DatabaseValidationTests {

        @Test
        @DisplayName("Should reject in-memory H2 database URL in production")
        void shouldRejectInMemoryH2Database() {
            validator.setDatasourceUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Database]") && e.contains("In-memory database URL")));
        }

        @Test
        @DisplayName("Should reject placeholder in database password")
        void shouldRejectPlaceholderDatabasePassword() {
            validator.setDatasourcePassword("YOUR_STRONG_DB_PASSWORD_HERE");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Database]") && e.contains("placeholder")));
        }

        @Test
        @DisplayName("Should reject short database password (< 8 chars)")
        void shouldRejectShortDatabasePassword() {
            validator.setDatasourcePassword("pass1");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Database]") && e.contains("at least 8 characters")));
        }

        @Test
        @DisplayName("Should reject missing database credentials")
        void shouldRejectMissingDatabaseCredentials() {
            validator.setDatasourceUrl("");
            validator.setDatasourceUsername(null);
            validator.setDatasourcePassword("");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Database] 'spring.datasource.url'")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Database] 'spring.datasource.username'")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Database] 'spring.datasource.password'")));
        }
    }

    @Nested
    @DisplayName("Redis Validation")
    class RedisValidationTests {

        @Test
        @DisplayName("Should reject localhost Redis host in production")
        void shouldRejectLocalhostRedisHost() {
            validator.setRedisHost("localhost");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Redis]") && e.contains("must not point to localhost")));

            validator.setRedisHost("127.0.0.1");
            errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Redis]") && e.contains("must not point to localhost")));
        }

        @Test
        @DisplayName("Should reject placeholder Redis password")
        void shouldRejectPlaceholderRedisPassword() {
            validator.setRedisPassword("YOUR_STRONG_REDIS_PASSWORD_HERE");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Redis]") && e.contains("placeholder")));
        }

        @Test
        @DisplayName("Should reject missing Redis password")
        void shouldRejectMissingRedisPassword() {
            validator.setRedisPassword("");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Redis]") && e.contains("authentication is required")));
        }

        @Test
        @DisplayName("Should reject invalid Redis port")
        void shouldRejectInvalidRedisPort() {
            validator.setRedisPort(0);
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Redis]") && e.contains("between 1 and 65535")));

            validator.setRedisPort(70000);
            errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Redis]") && e.contains("between 1 and 65535")));
        }
    }

    @Nested
    @DisplayName("CORS Validation")
    class CorsValidationTests {

        @Test
        @DisplayName("Should reject localhost and 127.0.0.1 origins in production")
        void shouldRejectLocalhostCorsOrigins() {
            validator.setCorsAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[CORS]") && e.contains("localhost")));
        }

        @Test
        @DisplayName("Should reject wildcard origin '*' in production")
        void shouldRejectWildcardCorsOrigin() {
            validator.setCorsAllowedOrigins(List.of("*"));
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[CORS]") && e.contains("Wildcard origin '*'")));
        }

        @Test
        @DisplayName("Should reject non-HTTPS origin in production")
        void shouldRejectNonHttpsCorsOrigin() {
            validator.setCorsAllowedOrigins(List.of("http://example.com"));
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[CORS]") && e.contains("HTTPS scheme")));
        }

        @Test
        @DisplayName("Should reject trailing slash in CORS origin")
        void shouldRejectTrailingSlashInCorsOrigin() {
            validator.setCorsAllowedOrigins(List.of("https://example.com/"));
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[CORS]") && e.contains("trailing slash")));
        }

        @Test
        @DisplayName("Should reject empty CORS origins list")
        void shouldRejectEmptyCorsOrigins() {
            validator.setCorsAllowedOrigins(List.of());
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[CORS]") && e.contains("must be explicitly configured")));
        }

        @Test
        @DisplayName("Should accept valid HTTPS origins with subdomains")
        void shouldAcceptValidHttpsOrigins() {
            validator.setCorsAllowedOrigins(List.of("https://app.example.com", "https://admin.example.com"));
            List<String> errors = validator.validate();
            assertFalse(errors.stream().anyMatch(e -> e.contains("[CORS]")));
        }
    }

    @Nested
    @DisplayName("Cookie and Rate Limiting Settings")
    class CookieAndPolicyValidationTests {

        @Test
        @DisplayName("Should reject cookieSecure=false in production")
        void shouldRejectInsecureCookie() {
            validator.setCookieSecure(false);
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Cookie]") && e.contains("MUST be true")));
        }

        @Test
        @DisplayName("Should reject invalid SameSite value")
        void shouldRejectInvalidSameSite() {
            validator.setCookieSameSite("InvalidSameSite");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Cookie]") && e.contains("Strict', 'Lax', or 'None'")));
        }

        @Test
        @DisplayName("Should reject invalid Redis rate limit failure policy")
        void shouldRejectInvalidRedisFailurePolicy() {
            validator.setRedisFailurePolicy("INVALID_POLICY");
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[RateLimiter]") && e.contains("FAIL_CLOSED or FAIL_OPEN")));
        }

        @Test
        @DisplayName("Should accept FAIL_OPEN and FAIL_CLOSED policies")
        void shouldAcceptValidFailurePolicies() {
            validator.setRedisFailurePolicy("FAIL_CLOSED");
            assertFalse(validator.validate().stream().anyMatch(e -> e.contains("[RateLimiter]")));

            validator.setRedisFailurePolicy("FAIL_OPEN");
            assertFalse(validator.validate().stream().anyMatch(e -> e.contains("[RateLimiter]")));
        }

        @Test
        @DisplayName("Should reject enabled payment simulation in production")
        void shouldRejectEnabledPaymentSimulation() {
            validator.setPaymentSimulationEnabled(true);
            List<String> errors = validator.validate();
            assertTrue(errors.stream().anyMatch(e -> e.contains("[Payment]") && e.contains("MUST NOT be enabled")));
        }
    }

    @Test
    @DisplayName("Should aggregate multiple violations into a single exception on startup")
    void shouldAggregateMultipleViolations() {
        validator.setJwtAccessSecret("CHANGE_ME");
        validator.setAdminPassword("Password@123");
        validator.setCorsAllowedOrigins(List.of("http://localhost:5173"));
        validator.setRedisHost("localhost");
        validator.setCookieSecure(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.validateOnStartup());
        String msg = ex.getMessage();

        assertTrue(msg.contains("[JWT]"));
        assertTrue(msg.contains("[Admin]"));
        assertTrue(msg.contains("[CORS]"));
        assertTrue(msg.contains("[Redis]"));
        assertTrue(msg.contains("[Cookie]"));
    }
}
