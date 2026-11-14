package com.japaneselearning.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditDataRedactor Unit Tests")
class AuditDataRedactorTest {

    private final AuditDataRedactor redactor = new AuditDataRedactor(new ObjectMapper());

    @Test
    @DisplayName("Returns empty map when input is null or empty")
    void sanitize_NullOrEmpty_ReturnsEmpty() {
        assertThat(redactor.sanitize(null)).isEmpty();
        assertThat(redactor.sanitize(Map.of())).isEmpty();
    }

    @Test
    @DisplayName("Redacts sensitive keys regardless of case and suffixes")
    void sanitize_SensitiveKeys_Masked() {
        Map<String, Object> input = new HashMap<>();
        input.put("password", "secret123");
        input.put("currentPassword", "oldSecret123");
        input.put("confirmPassword", "oldSecret123");
        input.put("client_secret", "oauth-secret");
        input.put("accessToken", "my-access-token");
        input.put("refreshToken", "my-refresh-token");
        input.put("sessionCookie", "JSESSIONID=12345");
        input.put("cvv", "123");
        input.put("cardNumber", "4111222233334444");
        input.put("vnp_hashSecret", "secret-vnp");
        input.put("nonSensitive", "keep-me");

        Map<String, Object> sanitized = redactor.sanitize(input);

        assertThat(sanitized.get("password")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("currentPassword")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("confirmPassword")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("client_secret")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("accessToken")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("refreshToken")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("sessionCookie")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("cvv")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("cardNumber")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("vnp_hashSecret")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("nonSensitive")).isEqualTo("keep-me");
    }

    @Test
    @DisplayName("Redacts sensitive string values like JWT tokens and credit cards in unsuspected keys")
    void sanitize_SensitiveValuesInGenericKeys_Masked() {
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String creditCard = "4111222233334444";

        Map<String, Object> input = Map.of(
                "customData", jwtToken,
                "paymentRef", creditCard,
                "courseTitle", "N5 Grammar Master"
        );

        Map<String, Object> sanitized = redactor.sanitize(input);

        assertThat(sanitized.get("customData")).isEqualTo("[REDACTED_JWT]");
        assertThat(sanitized.get("paymentRef")).isEqualTo("[REDACTED_CARD]");
        assertThat(sanitized.get("courseTitle")).isEqualTo("N5 Grammar Master");
    }

    @Test
    @DisplayName("Recursively redacts nested maps and lists")
    void sanitize_NestedStructures_RedactedRecursively() {
        Map<String, Object> innerMap = Map.of(
                "subKey", "safeValue",
                "nestedPassword", "p@ssw0rd!"
        );

        List<Object> list = List.of(
                "regularString",
                Map.of("token", "bearer-xyz", "id", 101)
        );

        Map<String, Object> input = Map.of(
                "user", innerMap,
                "items", list,
                "topLevelSafe", 42
        );

        Map<String, Object> sanitized = redactor.sanitize(input);

        @SuppressWarnings("unchecked")
        Map<String, Object> redactedInner = (Map<String, Object>) sanitized.get("user");
        assertThat(redactedInner.get("subKey")).isEqualTo("safeValue");
        assertThat(redactedInner.get("nestedPassword")).isEqualTo("[REDACTED]");

        @SuppressWarnings("unchecked")
        List<Object> redactedList = (List<Object>) sanitized.get("items");
        assertThat(redactedList.get(0)).isEqualTo("regularString");

        @SuppressWarnings("unchecked")
        Map<String, Object> listItemMap = (Map<String, Object>) redactedList.get(1);
        assertThat(listItemMap.get("token")).isEqualTo("[REDACTED]");
        assertThat(listItemMap.get("id")).isEqualTo(101);

        assertThat(sanitized.get("topLevelSafe")).isEqualTo(42);
    }

    @Test
    @DisplayName("Serializes safe metadata to JSON string")
    void toJsonString_ValidMap_ReturnsJson() {
        Map<String, Object> input = Map.of(
                "action", "LOGIN",
                "password", "secret",
                "email", "user@example.com"
        );

        String json = redactor.toJsonString(input);

        assertThat(json).contains("\"email\":\"user@example.com\"");
        assertThat(json).contains("\"password\":\"[REDACTED]\"");
        assertThat(json).doesNotContain("secret");
    }
}
