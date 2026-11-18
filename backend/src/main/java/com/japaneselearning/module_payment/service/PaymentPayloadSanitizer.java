package com.japaneselearning.module_payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Sanitizer for payment webhook responses, raw payment records, and audit metadata.
 * Enforces strict whitelisting of reconciliation/support fields and redaction of
 * cryptographic hashes, credentials, and personal data (e.g., client IPs).
 */
@Component
@Slf4j
public class PaymentPayloadSanitizer {

    private final ObjectMapper objectMapper;

    public PaymentPayloadSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Whitelist of VNPay parameter keys allowed for reconciliation and customer support.
     */
    public static final Set<String> ALLOWED_VNPAY_RECONCILIATION_KEYS = Set.of(
            "vnp_TxnRef",            // Order reference code
            "vnp_TransactionNo",     // VNPay transaction ID
            "vnp_Amount",            // Amount in cents
            "vnp_ResponseCode",      // Response code (00, 24, 11, etc.)
            "vnp_TransactionStatus",  // Transaction status
            "vnp_BankCode",          // Bank code (NCB, VCB, etc.)
            "vnp_BankTranNo",        // Bank transaction number
            "vnp_CardType",          // Payment card type (ATM, QRCODE, etc.)
            "vnp_PayDate",           // Payment timestamp from provider
            "vnp_OrderInfo"          // Sanitized order info description
    );

    /**
     * Forbidden sensitive keys that must never appear in raw payment records or audit logs.
     */
    public static final Set<String> SENSITIVE_FORBIDDEN_KEYS = Set.of(
            "vnp_SecureHash",
            "vnp_SecureHashType",
            "vnp_HashSecret",
            "vnp_TmnCode",
            "vnp_IpAddr",
            "hashSecret",
            "secureHash",
            "signature",
            "secretKey",
            "clientIp",
            "ipAddr"
    );

    private static final Pattern CARD_PATTERN = Pattern.compile(
            "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13})\\b"
    );

    /**
     * Sanitize a VNPay parameter map into a structured, redacted JSON string
     * containing strictly the whitelisted reconciliation and support fields.
     *
     * @param params Raw parameter map from webhook / IPN
     * @return Sanitized JSON string free from hashes, credentials, and personal data
     */
    public String sanitizeVnPayPayload(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "{}";
        }

        Map<String, Object> sanitized = new TreeMap<>();
        for (String key : ALLOWED_VNPAY_RECONCILIATION_KEYS) {
            String value = params.get(key);
            if (value != null && !value.isBlank()) {
                sanitized.put(key, value);
            }
        }

        try {
            return objectMapper.writeValueAsString(sanitized);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize sanitized VNPay payload: {}", e.getMessage());
            return "{\"error\":\"serialization_failed\"}";
        }
    }

    /**
     * Extract a clean map of reconciliation metadata safe for audit logging.
     *
     * @param params Raw parameter map
     * @return Clean Map containing only reconciliation fields
     */
    public Map<String, Object> extractReconciliationMetadata(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        for (String key : ALLOWED_VNPAY_RECONCILIATION_KEYS) {
            String value = params.get(key);
            if (value != null && !value.isBlank()) {
                metadata.put(key, value);
            }
        }
        return metadata;
    }

    /**
     * Sanitize generic webhook payload (JSON or key-value) to strip sensitive fields.
     */
    public String sanitizeGenericPayload(String rawBody, Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            Map<String, Object> sanitized = new TreeMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (isForbiddenKey(key)) {
                    continue;
                }
                sanitized.put(key, entry.getValue());
            }
            try {
                return objectMapper.writeValueAsString(sanitized);
            } catch (JsonProcessingException e) {
                return "{}";
            }
        }
        if (rawBody != null && !rawBody.isBlank()) {
            return redactRawString(rawBody);
        }
        return "{}";
    }

    /**
     * Check if a parameter key is sensitive and must be excluded.
     */
    public boolean isForbiddenKey(String key) {
        if (key == null) return false;
        String lower = key.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        return lower.contains("securehash") ||
               lower.contains("hashsecret") ||
               lower.contains("secret") ||
               lower.contains("signature") ||
               lower.contains("password") ||
               lower.contains("token") ||
               lower.contains("ipaddr") ||
               lower.contains("clientip") ||
               lower.contains("tmncode") ||
               lower.contains("cvv") ||
               lower.contains("cardnumber");
    }

    /**
     * Redact sensitive tokens and card patterns from raw string text.
     */
    public String redactRawString(String text) {
        if (text == null) return null;
        String result = text;
        result = result.replaceAll("(?i)vnp_SecureHash=[^&\\s,}\"]*", "vnp_SecureHash=[REDACTED]");
        result = result.replaceAll("(?i)vnp_SecureHashType=[^&\\s,}\"]*", "vnp_SecureHashType=[REDACTED]");
        result = result.replaceAll("(?i)vnp_IpAddr=[^&\\s,}\"]*", "vnp_IpAddr=[REDACTED]");
        result = result.replaceAll("(?i)vnp_TmnCode=[^&\\s,}\"]*", "vnp_TmnCode=[REDACTED]");
        result = CARD_PATTERN.matcher(result).replaceAll("[REDACTED_CARD]");
        return result;
    }
}
