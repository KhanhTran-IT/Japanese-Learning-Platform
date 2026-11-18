package com.japaneselearning.module_payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentPayloadSanitizer Unit Tests")
class PaymentPayloadSanitizerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentPayloadSanitizer sanitizer = new PaymentPayloadSanitizer(objectMapper);

    @Test
    @DisplayName("sanitizeVnPayPayload: Preserves whitelisted reconciliation fields and strips sensitive fields")
    void sanitizeVnPayPayload_StripsSensitiveFieldsAndPreservesReconciliation() throws Exception {
        Map<String, String> input = new HashMap<>();
        input.put("vnp_TxnRef", "ORD-20261118-001");
        input.put("vnp_TransactionNo", "14123456");
        input.put("vnp_Amount", "90000000");
        input.put("vnp_ResponseCode", "00");
        input.put("vnp_TransactionStatus", "00");
        input.put("vnp_BankCode", "NCB");
        input.put("vnp_BankTranNo", "VNP12345678");
        input.put("vnp_CardType", "ATM");
        input.put("vnp_PayDate", "20261118080000");
        input.put("vnp_OrderInfo", "Thanh toan don hang ORD-20261118-001");

        // Sensitive & unnecessary fields that MUST be stripped:
        input.put("vnp_SecureHash", "3b84f23498acde12903847f98124abcdef");
        input.put("vnp_SecureHashType", "HMACSHA512");
        input.put("vnp_TmnCode", "DEMOTMN1");
        input.put("vnp_IpAddr", "192.168.1.100");
        input.put("extra_customer_note", "Personal customer note");
        input.put("customer_phone", "0901234567");

        String json = sanitizer.sanitizeVnPayPayload(input);
        JsonNode node = objectMapper.readTree(json);

        // Assert whitelisted fields are present and intact
        assertThat(node.path("vnp_TxnRef").asText()).isEqualTo("ORD-20261118-001");
        assertThat(node.path("vnp_TransactionNo").asText()).isEqualTo("14123456");
        assertThat(node.path("vnp_Amount").asText()).isEqualTo("90000000");
        assertThat(node.path("vnp_ResponseCode").asText()).isEqualTo("00");
        assertThat(node.path("vnp_TransactionStatus").asText()).isEqualTo("00");
        assertThat(node.path("vnp_BankCode").asText()).isEqualTo("NCB");
        assertThat(node.path("vnp_BankTranNo").asText()).isEqualTo("VNP12345678");
        assertThat(node.path("vnp_CardType").asText()).isEqualTo("ATM");
        assertThat(node.path("vnp_PayDate").asText()).isEqualTo("20261118080000");
        assertThat(node.path("vnp_OrderInfo").asText()).isEqualTo("Thanh toan don hang ORD-20261118-001");

        // Assert sensitive & unnecessary fields are completely absent
        assertThat(node.has("vnp_SecureHash")).isFalse();
        assertThat(node.has("vnp_SecureHashType")).isFalse();
        assertThat(node.has("vnp_TmnCode")).isFalse();
        assertThat(node.has("vnp_IpAddr")).isFalse();
        assertThat(node.has("extra_customer_note")).isFalse();
        assertThat(node.has("customer_phone")).isFalse();

        // Also assert raw string doesn't contain sensitive values
        assertThat(json).doesNotContain("3b84f23498acde12903847f98124abcdef");
        assertThat(json).doesNotContain("192.168.1.100");
        assertThat(json).doesNotContain("DEMOTMN1");
    }

    @Test
    @DisplayName("sanitizeVnPayPayload: Empty or null map returns empty JSON object")
    void sanitizeVnPayPayload_NullOrEmpty() {
        assertThat(sanitizer.sanitizeVnPayPayload(null)).isEqualTo("{}");
        assertThat(sanitizer.sanitizeVnPayPayload(Map.of())).isEqualTo("{}");
    }

    @Test
    @DisplayName("extractReconciliationMetadata: Returns clean map with only reconciliation fields")
    void extractReconciliationMetadata_ExtractsOnlyAllowed() {
        Map<String, String> input = Map.of(
                "vnp_TxnRef", "ORD-999",
                "vnp_TransactionNo", "8888",
                "vnp_SecureHash", "secret-hash",
                "vnp_IpAddr", "10.0.0.1"
        );

        Map<String, Object> metadata = sanitizer.extractReconciliationMetadata(input);

        assertThat(metadata).containsEntry("vnp_TxnRef", "ORD-999");
        assertThat(metadata).containsEntry("vnp_TransactionNo", "8888");
        assertThat(metadata).doesNotContainKey("vnp_SecureHash");
        assertThat(metadata).doesNotContainKey("vnp_IpAddr");
    }

    @Test
    @DisplayName("isForbiddenKey: Accurately identifies sensitive key variations")
    void isForbiddenKey_IdentifiesSensitiveVariants() {
        assertThat(sanitizer.isForbiddenKey("vnp_SecureHash")).isTrue();
        assertThat(sanitizer.isForbiddenKey("vnp_securehash")).isTrue();
        assertThat(sanitizer.isForbiddenKey("vnp_SecureHashType")).isTrue();
        assertThat(sanitizer.isForbiddenKey("vnp_TmnCode")).isTrue();
        assertThat(sanitizer.isForbiddenKey("vnp_IpAddr")).isTrue();
        assertThat(sanitizer.isForbiddenKey("client_ip")).isTrue();
        assertThat(sanitizer.isForbiddenKey("hashSecret")).isTrue();
        assertThat(sanitizer.isForbiddenKey("cardNumber")).isTrue();

        assertThat(sanitizer.isForbiddenKey("vnp_TxnRef")).isFalse();
        assertThat(sanitizer.isForbiddenKey("vnp_TransactionNo")).isFalse();
        assertThat(sanitizer.isForbiddenKey("vnp_BankCode")).isFalse();
    }
}
