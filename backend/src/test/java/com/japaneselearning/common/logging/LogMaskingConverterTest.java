package com.japaneselearning.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link LogMaskingConverter}.
 * Verifies that sensitive data patterns (passwords, JWTs, tokens, cookies) are properly redacted.
 */
class LogMaskingConverterTest {

    private final LogMaskingConverter converter = new LogMaskingConverter();

    // ==========================================
    // PASSWORD MASKING
    // ==========================================

    @Test
    @DisplayName("Should mask JSON password fields")
    void shouldMaskJsonPasswordField() {
        String input = "{\"password\":\"mySecretPass123\",\"email\":\"user@test.com\"}";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).contains("\"password\":\"***REDACTED***\"");
        assertThat(result).doesNotContain("mySecretPass123");
        assertThat(result).contains("user@test.com");
    }

    @Test
    @DisplayName("Should mask currentPassword, newPassword, confirmPassword fields")
    void shouldMaskAllPasswordVariants() {
        String input = "{\"currentPassword\":\"oldSecret123\",\"newPassword\":\"myNewSecret456\",\"confirmPassword\":\"myNewSecret456\"}";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("oldSecret123");
        assertThat(result).doesNotContain("myNewSecret456");
        assertThat(result).contains("***REDACTED***");
    }

    @Test
    @DisplayName("Should mask form-encoded password fields")
    void shouldMaskFormEncodedPassword() {
        String input = "password=mySecret&email=user@test.com";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).contains("password=***REDACTED***");
        assertThat(result).doesNotContain("mySecret");
    }

    @Test
    @DisplayName("Should mask passwordHash field")
    void shouldMaskPasswordHash() {
        String input = "{\"passwordHash\":\"$2a$10$HashedPasswordValue\"}";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("$2a$10$HashedPasswordValue");
        assertThat(result).contains("***REDACTED***");
    }

    // ==========================================
    // BEARER TOKEN MASKING
    // ==========================================

    @Test
    @DisplayName("Should mask Bearer JWT tokens")
    void shouldMaskBearerToken() {
        String input = "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIn0.abc123signature";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).contains("Bearer ***REDACTED***");
        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiJ9");
    }

    // ==========================================
    // REFRESH TOKEN / TOKEN MASKING
    // ==========================================

    @Test
    @DisplayName("Should mask JSON refreshToken fields")
    void shouldMaskRefreshToken() {
        String input = "{\"refreshToken\":\"eyJhbGciOiJIUzI1NiJ9.payload.signature\"}";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).contains("\"refreshToken\":\"***REDACTED***\"");
        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiJ9");
    }

    @Test
    @DisplayName("Should mask tokenHash field")
    void shouldMaskTokenHash() {
        String input = "{\"tokenHash\":\"a1b2c3d4e5f6789012345\"}";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("a1b2c3d4e5f6789012345");
        assertThat(result).contains("***REDACTED***");
    }

    // ==========================================
    // COOKIE MASKING
    // ==========================================

    @Test
    @DisplayName("Should mask cookie refreshToken values")
    void shouldMaskCookieRefreshToken() {
        String input = "refreshToken=eyJhbGciOiJIUzI1NiJ9.payload.signature; Path=/; HttpOnly";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).contains("refreshToken=***REDACTED***");
        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiJ9");
    }

    @Test
    @DisplayName("Should mask Cookie header lines")
    void shouldMaskCookieHeader() {
        String input = "Cookie: refreshToken=abc123; JSESSIONID=xyz789";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).contains("Cookie: ***REDACTED***");
        assertThat(result).doesNotContain("abc123");
    }

    // ==========================================
    // FAST PATH (NON-SENSITIVE MESSAGES)
    // ==========================================

    @Test
    @DisplayName("Should return non-sensitive messages unchanged (fast path)")
    void shouldReturnNonSensitiveMessagesUnchanged() {
        String input = "User registered successfully: user@test.com";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    @DisplayName("Should handle null and empty messages")
    void shouldHandleNullAndEmpty() {
        assertThat(LogMaskingConverter.maskSensitiveData("")).isEmpty();
    }

    // ==========================================
    // CONVERTER INTEGRATION (via ILoggingEvent mock)
    // ==========================================

    @Test
    @DisplayName("Should mask via convert() method with ILoggingEvent")
    void shouldMaskViaConvertMethod() {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn("{\"password\":\"secret\"}");

        String result = converter.convert(event);

        assertThat(result).doesNotContain("secret");
        assertThat(result).contains("***REDACTED***");
    }

    @Test
    @DisplayName("Should handle null formatted message in ILoggingEvent")
    void shouldHandleNullFormattedMessage() {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getFormattedMessage()).thenReturn(null);

        String result = converter.convert(event);

        assertThat(result).isNull();
    }

    // ==========================================
    // COMBINED SCENARIOS
    // ==========================================

    @Test
    @DisplayName("Should mask multiple sensitive fields in one message")
    void shouldMaskMultipleSensitiveFields() {
        String input = "Request body: {\"password\":\"secret\",\"refreshToken\":\"abc123\"}";
        String result = LogMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("secret");
        assertThat(result).doesNotContain("abc123");
    }
}
