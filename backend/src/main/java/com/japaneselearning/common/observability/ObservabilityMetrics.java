package com.japaneselearning.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Centralized business metrics service for observability.
 * <p>
 * Provides pre-built counters and timers for key business events that can be
 * scraped by Prometheus via {@code /actuator/prometheus} and visualized in Grafana.
 *
 * <h3>Metric naming convention:</h3>
 * <ul>
 *     <li>{@code app_auth_failures_total} — Authentication failure counter</li>
 *     <li>{@code app_rate_limit_exceeded_total} — Rate limiting trigger counter</li>
 *     <li>{@code app_quiz_submissions_total} — Quiz submission counter</li>
 *     <li>{@code app_order_processing_total} — Order processing counter</li>
 *     <li>{@code app_request_duration_seconds} — HTTP request duration timer</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * Inject this service into any business service and call the appropriate method
 * when the event occurs. Metrics are thread-safe (atomic Micrometer counters).
 *
 * @see io.micrometer.core.instrument.MeterRegistry
 */
@Component
public class ObservabilityMetrics {

    private final MeterRegistry meterRegistry;

    // ---- Pre-registered counters ----
    private final Counter authFailureInvalidCredentials;
    private final Counter authFailureAccountLocked;
    private final Counter authFailureExpiredToken;
    private final Counter authFailureInvalidToken;

    private final Counter rateLimitLoginExceeded;
    private final Counter rateLimitRefreshExceeded;

    public ObservabilityMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Authentication failure counters
        this.authFailureInvalidCredentials = Counter.builder("app.auth.failures")
                .description("Authentication failures")
                .tag("reason", "invalid_credentials")
                .register(meterRegistry);

        this.authFailureAccountLocked = Counter.builder("app.auth.failures")
                .description("Authentication failures")
                .tag("reason", "account_locked")
                .register(meterRegistry);

        this.authFailureExpiredToken = Counter.builder("app.auth.failures")
                .description("Authentication failures")
                .tag("reason", "expired_token")
                .register(meterRegistry);

        this.authFailureInvalidToken = Counter.builder("app.auth.failures")
                .description("Authentication failures")
                .tag("reason", "invalid_token")
                .register(meterRegistry);

        // Rate limiting counters
        this.rateLimitLoginExceeded = Counter.builder("app.rate.limit.exceeded")
                .description("Rate limit exceeded events")
                .tag("type", "login")
                .register(meterRegistry);

        this.rateLimitRefreshExceeded = Counter.builder("app.rate.limit.exceeded")
                .description("Rate limit exceeded events")
                .tag("type", "refresh")
                .register(meterRegistry);
    }

    // ==========================================
    // AUTHENTICATION METRICS
    // ==========================================

    public void recordAuthFailureInvalidCredentials() {
        authFailureInvalidCredentials.increment();
    }

    public void recordAuthFailureAccountLocked() {
        authFailureAccountLocked.increment();
    }

    public void recordAuthFailureExpiredToken() {
        authFailureExpiredToken.increment();
    }

    public void recordAuthFailureInvalidToken() {
        authFailureInvalidToken.increment();
    }

    // ==========================================
    // RATE LIMITING METRICS
    // ==========================================

    public void recordRateLimitExceeded(String type) {
        switch (type) {
            case "login" -> rateLimitLoginExceeded.increment();
            case "refresh" -> rateLimitRefreshExceeded.increment();
            default -> Counter.builder("app.rate.limit.exceeded")
                    .tag("type", type)
                    .register(meterRegistry)
                    .increment();
        }
    }

    // ==========================================
    // QUIZ METRICS
    // ==========================================

    /**
     * Records a quiz submission with pass/fail outcome.
     *
     * @param quizId the quiz ID
     * @param passed whether the student passed
     */
    public void recordQuizSubmission(Long quizId, boolean passed) {
        Counter.builder("app.quiz.submissions")
                .description("Quiz submissions")
                .tag("quiz_id", String.valueOf(quizId))
                .tag("passed", String.valueOf(passed))
                .register(meterRegistry)
                .increment();
    }

    // ==========================================
    // ORDER / PAYMENT METRICS
    // ==========================================

    /**
     * Records an order processing event.
     *
     * @param status the order status (PAID, CANCELLED, FAILED, etc.)
     * @param paymentMethod the payment method used (SIMULATION, VNPAY, etc.)
     */
    public void recordOrderProcessed(String status, String paymentMethod) {
        Counter.builder("app.order.processing")
                .description("Order processing events")
                .tag("status", status)
                .tag("payment_method", paymentMethod != null ? paymentMethod : "unknown")
                .register(meterRegistry)
                .increment();
    }

    // ==========================================
    // REQUEST METRICS
    // ==========================================

    /**
     * Records HTTP request duration for custom timing beyond what Actuator provides.
     *
     * @param method HTTP method
     * @param uri request URI pattern
     * @param status HTTP status code
     * @param durationMs duration in milliseconds
     */
    public void recordRequestDuration(String method, String uri, int status, long durationMs) {
        Timer.builder("app.request.duration")
                .description("HTTP request duration")
                .tag("method", method)
                .tag("uri", uri)
                .tag("status", String.valueOf(status))
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
