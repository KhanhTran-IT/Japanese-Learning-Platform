package com.japaneselearning.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ObservabilityMetrics}.
 * Verifies that business metrics are correctly incremented with proper tags.
 */
class ObservabilityMetricsTest {

    private MeterRegistry meterRegistry;
    private ObservabilityMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new ObservabilityMetrics(meterRegistry);
    }

    // ==========================================
    // AUTH FAILURE METRICS
    // ==========================================

    @Test
    @DisplayName("Should increment auth failure counter for invalid credentials")
    void shouldRecordInvalidCredentials() {
        metrics.recordAuthFailureInvalidCredentials();
        metrics.recordAuthFailureInvalidCredentials();

        Counter counter = meterRegistry.find("app.auth.failures")
                .tag("reason", "invalid_credentials")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Should increment auth failure counter for account locked")
    void shouldRecordAccountLocked() {
        metrics.recordAuthFailureAccountLocked();

        Counter counter = meterRegistry.find("app.auth.failures")
                .tag("reason", "account_locked")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should increment auth failure counter for expired token")
    void shouldRecordExpiredToken() {
        metrics.recordAuthFailureExpiredToken();

        Counter counter = meterRegistry.find("app.auth.failures")
                .tag("reason", "expired_token")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should increment auth failure counter for invalid token")
    void shouldRecordInvalidToken() {
        metrics.recordAuthFailureInvalidToken();

        Counter counter = meterRegistry.find("app.auth.failures")
                .tag("reason", "invalid_token")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Auth failure counters should be independent per reason tag")
    void authFailureCountersShouldBeIndependent() {
        metrics.recordAuthFailureInvalidCredentials();
        metrics.recordAuthFailureInvalidCredentials();
        metrics.recordAuthFailureAccountLocked();

        double invalidCreds = meterRegistry.find("app.auth.failures")
                .tag("reason", "invalid_credentials").counter().count();
        double locked = meterRegistry.find("app.auth.failures")
                .tag("reason", "account_locked").counter().count();

        assertThat(invalidCreds).isEqualTo(2.0);
        assertThat(locked).isEqualTo(1.0);
    }

    // ==========================================
    // RATE LIMITING METRICS
    // ==========================================

    @Test
    @DisplayName("Should increment rate limit counter for login type")
    void shouldRecordLoginRateLimit() {
        metrics.recordRateLimitExceeded("login");

        Counter counter = meterRegistry.find("app.rate.limit.exceeded")
                .tag("type", "login")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should increment rate limit counter for refresh type")
    void shouldRecordRefreshRateLimit() {
        metrics.recordRateLimitExceeded("refresh");

        Counter counter = meterRegistry.find("app.rate.limit.exceeded")
                .tag("type", "refresh")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    // ==========================================
    // QUIZ SUBMISSION METRICS
    // ==========================================

    @Test
    @DisplayName("Should record quiz submission with passed=true")
    void shouldRecordQuizPassed() {
        metrics.recordQuizSubmission(1L, true);

        Counter counter = meterRegistry.find("app.quiz.submissions")
                .tag("quiz_id", "1")
                .tag("passed", "true")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should record quiz submission with passed=false")
    void shouldRecordQuizFailed() {
        metrics.recordQuizSubmission(42L, false);

        Counter counter = meterRegistry.find("app.quiz.submissions")
                .tag("quiz_id", "42")
                .tag("passed", "false")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    // ==========================================
    // ORDER PROCESSING METRICS
    // ==========================================

    @Test
    @DisplayName("Should record order processed with PAID status")
    void shouldRecordOrderPaid() {
        metrics.recordOrderProcessed("PAID", "SIMULATION");

        Counter counter = meterRegistry.find("app.order.processing")
                .tag("status", "PAID")
                .tag("payment_method", "SIMULATION")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should handle null payment method gracefully")
    void shouldHandleNullPaymentMethod() {
        metrics.recordOrderProcessed("CANCELLED", null);

        Counter counter = meterRegistry.find("app.order.processing")
                .tag("status", "CANCELLED")
                .tag("payment_method", "unknown")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }
}
