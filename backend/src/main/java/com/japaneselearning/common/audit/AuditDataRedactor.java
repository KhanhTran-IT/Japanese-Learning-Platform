package com.japaneselearning.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility for sanitizing and redacting sensitive credentials, tokens, cookies,
 * payment secrets, and personal information before audit persistence.
 */
@Component
@Slf4j
public class AuditDataRedactor {

    private final ObjectMapper objectMapper;

    public AuditDataRedactor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private static final String REDACTED_MASK = "[REDACTED]";
    private static final String REDACTED_JWT = "[REDACTED_JWT]";
    private static final String REDACTED_CARD = "[REDACTED_CARD]";

    /**
     * Case-insensitive keys that MUST be redacted entirely.
     */
    private static final Set<String> SENSITIVE_KEY_EXACT = Set.of(
            "password", "currentpassword", "newpassword", "confirmpassword", "passwordhash",
            "token", "accesstoken", "refreshtoken", "tokenhash", "token_hash", "jwt",
            "secret", "hashsecret", "vnp_hashsecret", "vnp_securehash", "signature",
            "cookie", "set-cookie", "authorization", "cvv", "cvv2", "cardnumber", "card_number",
            "privatekey", "secretkey"
    );

    /**
     * Substring patterns in keys indicating sensitive content.
     */
    private static final List<String> SENSITIVE_KEY_SUBSTRINGS = List.of(
            "password", "secret", "token", "hashsecret", "securehash", "privatekey", "cookie", "cvv", "card"
    );

    /**
     * Regex matching JWT format: eyJhbGciOi... or standard 3-part base64 strings.
     */
    private static final Pattern JWT_PATTERN = Pattern.compile(
            "(Bearer\\s+)?[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_=]+\\.[A-Za-z0-9\\-_.+/=]*",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Regex matching credit card numbers (13-19 digits).
     */
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13})\\b"
    );

    /**
     * Sanitize a metadata map by recursively redacting sensitive keys and values.
     *
     * @param input Raw metadata map
     * @return Sanitized map safe for storage
     */
    public Map<String, Object> sanitize(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (isSensitiveKey(key)) {
                sanitized.put(key, REDACTED_MASK);
            } else {
                sanitized.put(key, sanitizeValue(value));
            }
        }
        return sanitized;
    }

    /**
     * Convert metadata map to a sanitized JSON string.
     *
     * @param metadata Map of metadata
     * @return Sanitized JSON string, or null if empty
     */
    public String toJsonString(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        Map<String, Object> sanitized = sanitize(metadata);
        try {
            return objectMapper.writeValueAsString(sanitized);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit metadata to JSON: {}", e.getMessage());
            return "{\"error\":\"Serialization failure\"}";
        }
    }

    /**
     * Sanitize a raw string (e.g. JSON or message) by masking JWTs, card numbers, and known sensitive key-values.
     *
     * @param rawText Raw text content
     * @return Sanitized text
     */
    public String redactRawString(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return rawText;
        }

        String result = rawText;
        // Redact JWT tokens
        result = JWT_PATTERN.matcher(result).replaceAll(REDACTED_JWT);
        // Redact Credit card numbers
        result = CREDIT_CARD_PATTERN.matcher(result).replaceAll(REDACTED_CARD);

        return result;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String lowerKey = key.toLowerCase().replace("-", "").replace("_", "");
        if (SENSITIVE_KEY_EXACT.contains(lowerKey)) {
            return true;
        }
        for (String sub : SENSITIVE_KEY_SUBSTRINGS) {
            if (lowerKey.contains(sub)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map) {
            return sanitize((Map<String, Object>) value);
        }

        if (value instanceof Collection<?>) {
            List<Object> sanitizedList = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                sanitizedList.add(sanitizeValue(item));
            }
            return sanitizedList;
        }

        if (value instanceof String strVal) {
            return redactRawString(strVal);
        }

        return value;
    }
}
