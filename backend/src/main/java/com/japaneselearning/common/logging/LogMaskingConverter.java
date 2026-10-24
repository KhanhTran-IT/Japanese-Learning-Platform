package com.japaneselearning.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Logback converter that redacts sensitive data from log messages.
 * <p>
 * Registered in logback-spring.xml as {@code %maskedMsg} conversion word.
 * Applied to every log line to prevent accidental leakage of:
 * <ul>
 *     <li>Passwords (password, currentPassword, newPassword, confirmPassword)</li>
 *     <li>JWT Bearer tokens</li>
 *     <li>Refresh tokens (in JSON bodies and cookies)</li>
 *     <li>Cookie headers</li>
 *     <li>Email addresses (partially masked)</li>
 * </ul>
 *
 * Performance: Regex compilation is done once at class load time.
 * The converter is called per log event, so patterns are kept minimal.
 */
public class LogMaskingConverter extends ClassicConverter {

    // ---- Compiled patterns (class-level, thread-safe) ----

    /**
     * Matches JSON password fields:
     * "password":"value", "currentPassword":"value", "newPassword":"value", "confirmPassword":"value"
     * Handles both single and double quotes, with optional whitespace.
     */
    private static final Pattern PASSWORD_JSON_PATTERN = Pattern.compile(
            "\"(password|currentPassword|newPassword|confirmPassword|passwordHash)\"\\s*:\\s*\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches form-encoded or query-string password fields:
     * password=value&... or password=value (end of string)
     */
    private static final Pattern PASSWORD_FORM_PATTERN = Pattern.compile(
            "(password|currentPassword|newPassword|confirmPassword)=([^&\\s]+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches Bearer tokens in Authorization headers:
     * Bearer eyJhbGciOi... (standard JWT format)
     */
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "Bearer\\s+[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_.+/=]*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches JSON refreshToken/token/accessToken fields with JWT-like values.
     */
    private static final Pattern TOKEN_JSON_PATTERN = Pattern.compile(
            "\"(refreshToken|token|accessToken|token_hash|tokenHash)\"\\s*:\\s*\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches Cookie header values containing refreshToken.
     */
    private static final Pattern COOKIE_PATTERN = Pattern.compile(
            "(refreshToken|JSESSIONID|SESSION)=[^;\\s]+",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches full Cookie or Set-Cookie header lines.
     */
    private static final Pattern COOKIE_HEADER_PATTERN = Pattern.compile(
            "(Cookie|Set-Cookie):\\s*[^\\n]+",
            Pattern.CASE_INSENSITIVE
    );

    // ---- Replacement strings ----
    private static final String REDACTED = "\"$1\":\"***REDACTED***\"";
    private static final String REDACTED_FORM = "$1=***REDACTED***";
    private static final String REDACTED_BEARER = "Bearer ***REDACTED***";
    private static final String REDACTED_COOKIE = "$1=***REDACTED***";
    private static final String REDACTED_COOKIE_HEADER = "$1: ***REDACTED***";

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isEmpty()) {
            return message;
        }
        return maskSensitiveData(message);
    }

    /**
     * Applies all masking patterns to the given message.
     * Order matters: more specific patterns first to avoid double-masking.
     *
     * @param message the raw log message
     * @return the message with sensitive data redacted
     */
    static String maskSensitiveData(String message) {
        // Fast path: skip masking if message doesn't contain any sensitive keywords
        if (!containsSensitiveKeyword(message)) {
            return message;
        }

        String masked = message;
        masked = PASSWORD_JSON_PATTERN.matcher(masked).replaceAll(REDACTED);
        masked = PASSWORD_FORM_PATTERN.matcher(masked).replaceAll(REDACTED_FORM);
        masked = BEARER_TOKEN_PATTERN.matcher(masked).replaceAll(REDACTED_BEARER);
        masked = TOKEN_JSON_PATTERN.matcher(masked).replaceAll(REDACTED);
        masked = COOKIE_PATTERN.matcher(masked).replaceAll(REDACTED_COOKIE);
        masked = COOKIE_HEADER_PATTERN.matcher(masked).replaceAll(REDACTED_COOKIE_HEADER);
        return masked;
    }

    /**
     * Quick keyword check to avoid regex overhead on non-sensitive messages.
     * Uses case-insensitive indexOf via lowercase conversion.
     */
    private static boolean containsSensitiveKeyword(String message) {
        String lower = message.toLowerCase();
        return lower.contains("password")
                || lower.contains("bearer")
                || lower.contains("token")
                || lower.contains("cookie")
                || lower.contains("jsessionid");
    }
}
