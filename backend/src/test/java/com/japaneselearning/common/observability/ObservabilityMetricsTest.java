package com.japaneselearning.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ObservabilityMetrics}.
 * Verifies that business metrics are correctly incremented with low-cardinality tags,
 * entity identifiers (such as quiz_id) are excluded, and request URIs are normalized to stable route templates.
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

    @Test
    @DisplayName("Should sanitize high-cardinality rate limit types")
    void shouldSanitizeHighCardinalityRateLimitType() {
        metrics.recordRateLimitExceeded("user@example.com");
        metrics.recordRateLimitExceeded("192.168.1.100:8080");
        metrics.recordRateLimitExceeded(null);

        Counter customCounter = meterRegistry.find("app.rate.limit.exceeded")
                .tag("type", "custom")
                .counter();
        Counter unknownCounter = meterRegistry.find("app.rate.limit.exceeded")
                .tag("type", "unknown")
                .counter();

        assertThat(customCounter).isNotNull();
        assertThat(customCounter.count()).isEqualTo(2.0);
        assertThat(unknownCounter).isNotNull();
        assertThat(unknownCounter.count()).isEqualTo(1.0);
    }

    // ==========================================
    // QUIZ SUBMISSION METRICS (HIGH-CARDINALITY ELIMINATED)
    // ==========================================

    @Test
    @DisplayName("Should record quiz submission with passed=true without quiz_id tag")
    void shouldRecordQuizPassed() {
        metrics.recordQuizSubmission(true);

        Counter counter = meterRegistry.find("app.quiz.submissions")
                .tag("passed", "true")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        // Verify quiz_id tag does NOT exist on any app.quiz.submissions meter
        Collection<Counter> allQuizCounters = meterRegistry.find("app.quiz.submissions").counters();
        for (Counter c : allQuizCounters) {
            assertThat(c.getId().getTag("quiz_id")).isNull();
        }
    }

    @Test
    @DisplayName("Should record quiz submission with passed=false without quiz_id tag")
    void shouldRecordQuizFailed() {
        metrics.recordQuizSubmission(false);

        Counter counter = meterRegistry.find("app.quiz.submissions")
                .tag("passed", "false")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Collection<Counter> allQuizCounters = meterRegistry.find("app.quiz.submissions").counters();
        for (Counter c : allQuizCounters) {
            assertThat(c.getId().getTag("quiz_id")).isNull();
        }
    }

    @Test
    @DisplayName("Backward-compatible recordQuizSubmission(Long, boolean) ignores quiz_id and increments outcome counter")
    void shouldIgnoreQuizIdInBackwardCompatibleMethod() {
        metrics.recordQuizSubmission(101L, true);
        metrics.recordQuizSubmission(202L, true);
        metrics.recordQuizSubmission(303L, false);

        Counter passedCounter = meterRegistry.find("app.quiz.submissions")
                .tag("passed", "true")
                .counter();
        Counter failedCounter = meterRegistry.find("app.quiz.submissions")
                .tag("passed", "false")
                .counter();

        assertThat(passedCounter).isNotNull();
        assertThat(passedCounter.count()).isEqualTo(2.0);
        assertThat(failedCounter).isNotNull();
        assertThat(failedCounter.count()).isEqualTo(1.0);

        // Crucial: Registry must NOT have separate meters per quiz_id
        assertThat(meterRegistry.find("app.quiz.submissions").counters()).hasSize(2);
        for (Counter c : meterRegistry.find("app.quiz.submissions").counters()) {
            assertThat(c.getId().getTag("quiz_id")).isNull();
        }
    }

    // ==========================================
    // ORDER PROCESSING METRICS
    // ==========================================

    @Test
    @DisplayName("Should record order processed with PAID status and normalized payment method")
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
    @DisplayName("Should handle null and blank payment method and status gracefully")
    void shouldHandleNullPaymentMethod() {
        metrics.recordOrderProcessed("CANCELLED", null);
        metrics.recordOrderProcessed(null, "VNPAY");

        Counter counter1 = meterRegistry.find("app.order.processing")
                .tag("status", "CANCELLED")
                .tag("payment_method", "unknown")
                .counter();
        Counter counter2 = meterRegistry.find("app.order.processing")
                .tag("status", "UNKNOWN")
                .tag("payment_method", "VNPAY")
                .counter();

        assertThat(counter1).isNotNull();
        assertThat(counter1.count()).isEqualTo(1.0);
        assertThat(counter2).isNotNull();
        assertThat(counter2.count()).isEqualTo(1.0);
    }

    // ==========================================
    // REQUEST DURATION METRICS & URI NORMALIZATION
    // ==========================================

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "'/api/v1/courses/123', '/api/v1/courses/{id}'",
            "'/api/v1/courses/456', '/api/v1/courses/{id}'",
            "'/api/v1/courses/n5-grammar', '/api/v1/courses/{slug}'",
            "'/api/v1/courses/tieng-nhat-n3-cap-toc', '/api/v1/courses/{slug}'",
            "'/api/v1/courses', '/api/v1/courses'",
            "'/api/v1/courses/', '/api/v1/courses'",
            "'/api/v1/quizzes/42/submit', '/api/v1/quizzes/{id}/submit'",
            "'/api/v1/quizzes/99/result/105', '/api/v1/quizzes/{id}/result/{id}'",
            "'/api/v1/orders/15', '/api/v1/orders/{id}'",
            "'/api/v1/orders/ORD-20261121-ABCD', '/api/v1/orders/{id}'",
            "'/api/v1/orders/SIM-20261121-XYZ', '/api/v1/orders/{id}'",
            "'/api/v1/orders/me', '/api/v1/orders/me'",
            "'/api/v1/admin/users/88/lock', '/api/v1/admin/users/{id}/lock'",
            "'/api/v1/admin/sections/10/lessons/20', '/api/v1/admin/sections/{id}/lessons/{id}'",
            "'/api/v1/leaderboards?timeframe=WEEKLY&page=0', '/api/v1/leaderboards'",
            "'/api/v1/courses?level=N5#overview', '/api/v1/courses'",
            "'/api//v1///courses', '/api/v1/courses'",
            "'/api/files/123e4567-e89b-12d3-a456-426614174000', '/api/files/{id}'",
            "'/api/auth/verify/0123456789abcdef0123456789abcdef', '/api/auth/verify/{id}'",
            "'/', '/'",
            "'', 'UNKNOWN'",
            "'   ', 'UNKNOWN'"
    })
    @DisplayName("normalizeUri should replace dynamic segments with stable route templates")
    void shouldNormalizeUrisCorrectly(String rawUri, String expectedTemplate) {
        String normalized = ObservabilityMetrics.normalizeUri(rawUri);
        assertThat(normalized).isEqualTo(expectedTemplate);
    }

    @Test
    @DisplayName("recordRequestDuration normalizes URIs and methods preventing timeseries explosion")
    void shouldRecordRequestDurationWithNormalizedTags() {
        // Multiple requests to different IDs of the same resource
        metrics.recordRequestDuration("get", "/api/v1/courses/101", 200, 45);
        metrics.recordRequestDuration("GET", "/api/v1/courses/202?tab=lessons", 200, 60);
        metrics.recordRequestDuration("GET", "/api/v1/courses/303", 200, 55);

        Timer timer = meterRegistry.find("app.request.duration")
                .tag("method", "GET")
                .tag("uri", "/api/v1/courses/{id}")
                .tag("status", "200")
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(3);

        // Crucial: There must NOT be distinct timers for /courses/101, /courses/202, /courses/303
        assertThat(meterRegistry.find("app.request.duration").tag("uri", "/api/v1/courses/101").timer()).isNull();
        assertThat(meterRegistry.find("app.request.duration").tag("uri", "/api/v1/courses/202").timer()).isNull();
    }

    @Test
    @DisplayName("recordRequestDuration bounds invalid status and null method/URI gracefully")
    void shouldHandleEdgeCasesInRequestDuration() {
        metrics.recordRequestDuration(null, null, -1, 10);

        Timer timer = meterRegistry.find("app.request.duration")
                .tag("method", "UNKNOWN")
                .tag("uri", "UNKNOWN")
                .tag("status", "0")
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }
}
