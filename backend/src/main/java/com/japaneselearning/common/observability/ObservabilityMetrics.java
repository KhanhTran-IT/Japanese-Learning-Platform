package com.japaneselearning.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Centralized business metrics service for observability.
 * <p>
 * Provides pre-built counters and timers for key business events that can be
 * scraped by Prometheus via {@code /actuator/prometheus} and visualized in Grafana.
 * <p>
 * High-cardinality guard:
 * All metric labels are strictly restricted to low-cardinality values (enums, status strings, boolean outcomes).
 * Entity-specific identifiers (user IDs, quiz IDs, order IDs, account tokens) and unnormalized dynamic URIs
 * are strictly excluded from metric tags to prevent timeseries explosion in Prometheus.
 *
 * <h3>Metric naming convention:</h3>
 * <ul>
 *     <li>{@code app_auth_failures_total} — Authentication failure counter (reason)</li>
 *     <li>{@code app_rate_limit_exceeded_total} — Rate limiting trigger counter (type)</li>
 *     <li>{@code app_quiz_submissions_total} — Quiz submission counter (passed: true|false)</li>
 *     <li>{@code app_order_processing_total} — Order processing counter (status, payment_method)</li>
 *     <li>{@code app_request_duration_seconds} — HTTP request duration timer (method, normalized uri, status)</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * Inject this service into any business service and call the appropriate method
 * when the event occurs. Metrics are thread-safe (atomic Micrometer counters and timers).
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

    private final Counter quizSubmissionPassed;
    private final Counter quizSubmissionFailed;

    // Patterns for URI normalization to stable route templates
    private static final Pattern QUERY_OR_FRAGMENT_PATTERN = Pattern.compile("[?#].*");
    private static final Pattern MULTI_SLASH_PATTERN = Pattern.compile("/+");
    private static final Pattern UUID_PATTERN = Pattern.compile("/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(?=/|$)");
    private static final Pattern HEX_TOKEN_PATTERN = Pattern.compile("/[0-9a-fA-F]{16,}(?=/|$)");
    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("/(ORD|SIM)-[A-Za-z0-9_-]+(?=/|$)");
    private static final Pattern NUMERIC_ID_PATTERN = Pattern.compile("/\\d+(?=/|$)");
    private static final Pattern COURSE_SLUG_PATTERN = Pattern.compile("^/api/v1/courses/([^/]+)$");

    public ObservabilityMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Authentication failure counters (low-cardinality 'reason' tag)
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

        // Rate limiting counters (low-cardinality 'type' tag)
        this.rateLimitLoginExceeded = Counter.builder("app.rate.limit.exceeded")
                .description("Rate limit exceeded events")
                .tag("type", "login")
                .register(meterRegistry);

        this.rateLimitRefreshExceeded = Counter.builder("app.rate.limit.exceeded")
                .description("Rate limit exceeded events")
                .tag("type", "refresh")
                .register(meterRegistry);

        // Quiz submission counters (low-cardinality 'passed' tag, quiz_id omitted to prevent high cardinality)
        this.quizSubmissionPassed = Counter.builder("app.quiz.submissions")
                .description("Quiz submissions")
                .tag("passed", "true")
                .register(meterRegistry);

        this.quizSubmissionFailed = Counter.builder("app.quiz.submissions")
                .description("Quiz submissions")
                .tag("passed", "false")
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
        String safeType = normalizeRateLimitType(type);
        switch (safeType) {
            case "login" -> rateLimitLoginExceeded.increment();
            case "refresh" -> rateLimitRefreshExceeded.increment();
            default -> Counter.builder("app.rate.limit.exceeded")
                    .description("Rate limit exceeded events")
                    .tag("type", safeType)
                    .register(meterRegistry)
                    .increment();
        }
    }

    // ==========================================
    // QUIZ METRICS
    // ==========================================

    /**
     * Records a quiz submission with pass/fail outcome.
     * <p>
     * High-cardinality labels such as {@code quiz_id} or user IDs are strictly excluded
     * to eliminate timeseries explosion in Prometheus.
     *
     * @param passed whether the student passed
     */
    public void recordQuizSubmission(boolean passed) {
        if (passed) {
            quizSubmissionPassed.increment();
        } else {
            quizSubmissionFailed.increment();
        }
    }

    /**
     * Backward-compatible overload. Entity ID (quizId) is ignored to eliminate high cardinality.
     *
     * @param quizId ignored to prevent high-cardinality timeseries explosion
     * @param passed whether the student passed
     */
    public void recordQuizSubmission(Long quizId, boolean passed) {
        recordQuizSubmission(passed);
    }

    // ==========================================
    // ORDER / PAYMENT METRICS
    // ==========================================

    /**
     * Records an order processing event with low-cardinality status and payment method.
     *
     * @param status the order status (PAID, CANCELLED, FAILED, etc.)
     * @param paymentMethod the payment method used (SIMULATION, VNPAY, etc.)
     */
    public void recordOrderProcessed(String status, String paymentMethod) {
        String safeStatus = normalizeOrderStatus(status);
        String safePaymentMethod = normalizePaymentMethod(paymentMethod);

        Counter.builder("app.order.processing")
                .description("Order processing events")
                .tag("status", safeStatus)
                .tag("payment_method", safePaymentMethod)
                .register(meterRegistry)
                .increment();
    }

    // ==========================================
    // REQUEST METRICS
    // ==========================================

    /**
     * Records HTTP request duration for custom timing beyond what Actuator provides.
     * <p>
     * Request URIs are strictly normalized to stable route templates to eliminate
     * high-cardinality dynamic path parameters (numeric IDs, UUIDs, order codes, slugs, tokens, query params).
     *
     * @param method HTTP method (GET, POST, etc.)
     * @param uri request URI (raw path or template)
     * @param status HTTP status code (200, 404, etc.)
     * @param durationMs duration in milliseconds
     */
    public void recordRequestDuration(String method, String uri, int status, long durationMs) {
        String safeMethod = normalizeMethod(method);
        String safeUri = normalizeUri(uri);
        String safeStatus = normalizeStatus(status);

        Timer.builder("app.request.duration")
                .description("HTTP request duration")
                .tag("method", safeMethod)
                .tag("uri", safeUri)
                .tag("status", safeStatus)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    // ==========================================
    // NORMALIZATION & SANITIZATION HELPERS
    // ==========================================

    /**
     * Normalizes a request URI to a bounded, stable route template.
     * Strips query strings, anchors, and replaces dynamic segments (numeric IDs, UUIDs, order codes, slugs).
     *
     * @param uri raw request URI
     * @return normalized stable route template
     */
    public static String normalizeUri(String uri) {
        if (uri == null || uri.isBlank()) {
            return "UNKNOWN";
        }

        // 1. Strip query string and fragment
        String path = QUERY_OR_FRAGMENT_PATTERN.matcher(uri.trim()).replaceFirst("");

        // 2. Ensure leading slash and normalize multiple consecutive slashes
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        path = MULTI_SLASH_PATTERN.matcher(path).replaceAll("/");

        // 3. Remove trailing slash (keep root "/" intact)
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // 4. Replace UUIDs: /550e8400-e29b-41d4-a716-446655440000 -> /{id}
        path = UUID_PATTERN.matcher(path).replaceAll("/{id}");

        // 5. Replace long hex tokens: /a1b2c3d4e5f6071829... -> /{id}
        path = HEX_TOKEN_PATTERN.matcher(path).replaceAll("/{id}");

        // 6. Replace order codes: /ORD-20261121-001 -> /{id}
        path = ORDER_CODE_PATTERN.matcher(path).replaceAll("/{id}");

        // 7. Replace numeric segments: /123 -> /{id}
        path = NUMERIC_ID_PATTERN.matcher(path).replaceAll("/{id}");

        // 8. Normalize course slug route: /api/v1/courses/<slug> -> /api/v1/courses/{slug}
        Matcher courseMatcher = COURSE_SLUG_PATTERN.matcher(path);
        if (courseMatcher.matches()) {
            String slugCandidate = courseMatcher.group(1);
            if (!"{id}".equals(slugCandidate) && !"{slug}".equals(slugCandidate)) {
                path = "/api/v1/courses/{slug}";
            }
        }

        return path;
    }

    /**
     * Normalizes HTTP method to standard uppercase names, defaulting unknown values to 'OTHER' or 'UNKNOWN'.
     */
    public static String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "UNKNOWN";
        }
        String upper = method.trim().toUpperCase();
        return switch (upper) {
            case "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS" -> upper;
            default -> "OTHER";
        };
    }

    /**
     * Normalizes HTTP status code to string representation, bounding invalid codes to '0'.
     */
    public static String normalizeStatus(int status) {
        if (status < 100 || status > 599) {
            return "0";
        }
        return String.valueOf(status);
    }

    /**
     * Normalizes rate limit type, preventing high-cardinality strings from being injected into tags.
     */
    public static String normalizeRateLimitType(String type) {
        if (type == null || type.isBlank()) {
            return "unknown";
        }
        String trimmed = type.trim().toLowerCase();
        if ("login".equals(trimmed) || "refresh".equals(trimmed)) {
            return trimmed;
        }
        if (trimmed.contains("@") || trimmed.contains(":") || trimmed.contains("/") || trimmed.length() > 30) {
            return "custom";
        }
        return trimmed;
    }

    /**
     * Normalizes order status.
     */
    public static String normalizeOrderStatus(String status) {
        if (status == null || status.isBlank()) {
            return "UNKNOWN";
        }
        return status.trim().toUpperCase();
    }

    /**
     * Normalizes payment method.
     */
    public static String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "unknown";
        }
        return paymentMethod.trim().toUpperCase();
    }
}
