package com.japaneselearning.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Production Security Fail-Fast Integration Tests")
class ProductionSecurityFailFastIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ProductionSecurityValidator.class);

    private static final String VALID_ACCESS_SECRET = "NDBlNjM1MjY2NTVhNjg2ZTMyNzI1NzU4NzgvNDEzRjQ0Mjg0NzIrNEI2MlA2ZFM2N1Y2azlw";
    private static final String VALID_REFRESH_SECRET = "NTA2NDUzNjc1NmtiNTlwMzM3czM2NzYzOTc5MjQ0MjI2NDUyOTQ4NDA0RDYzNTE2NmU0NmE1";

    @Test
    @DisplayName("Validator bean should NOT be registered in default/dev profile")
    void validatorShouldNotBeActiveInNonProdProfiles() {
        contextRunner
                .withPropertyValues("spring.profiles.active=dev")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ProductionSecurityValidator.class);
                });
    }

    @Test
    @DisplayName("Context should fail fast when prod profile is active with placeholder values")
    void contextShouldFailFastWithPlaceholdersInProd() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=prod",
                        "spring.datasource.url=jdbc:mariadb://db:3306/japanese_learning_db",
                        "spring.datasource.username=root",
                        "spring.datasource.password=YOUR_STRONG_DB_PASSWORD_HERE",
                        "app.admin.password=YOUR_STRONG_ADMIN_PASSWORD_HERE",
                        "app.cors.allowed-origins=http://localhost:5173",
                        "jwt.secret.access=CHANGE_ME_GENERATE_WITH_OPENSSL_RAND_BASE64_32",
                        "jwt.secret.refresh=CHANGE_ME_GENERATE_WITH_OPENSSL_RAND_BASE64_32",
                        "spring.data.redis.host=localhost",
                        "spring.data.redis.password=YOUR_STRONG_REDIS_PASSWORD_HERE"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class);
                    assertThat(context.getStartupFailure().getCause())
                            .hasMessageContaining("Production configuration validation failed");
                });
    }

    @Test
    @DisplayName("Context should start successfully when all production properties are valid and secure")
    void contextShouldStartSuccessfullyWithValidProdConfig() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=prod",
                        "spring.datasource.url=jdbc:mariadb://db.internal:3306/japanese_learning_db?useSSL=true",
                        "spring.datasource.username=app_user",
                        "spring.datasource.password=ValidSecureDbPassword2026!",
                        "app.admin.password=ValidSecureAdminPassword2026!#",
                        "app.cors.allowed-origins=https://japaneselearning.com",
                        "jwt.secret.access=" + VALID_ACCESS_SECRET,
                        "jwt.secret.refresh=" + VALID_REFRESH_SECRET,
                        "spring.data.redis.host=redis.internal",
                        "spring.data.redis.port=6379",
                        "spring.data.redis.password=ValidSecureRedisPassword2026!",
                        "app.cookie.secure=true",
                        "app.cookie.same-site=Strict",
                        "app.rate-limit.redis-failure-policy=FAIL_CLOSED",
                        "app.payment.simulation-enabled=false"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ProductionSecurityValidator.class);
                });
    }
}
