package com.japaneselearning.module_payment.gateway.vnpay;

import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.gateway.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class VnPayPaymentGateway implements PaymentGateway {

    private final VnPayProperties properties;

    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public PaymentInitResult initPayment(PaymentInitCommand command) {
        String createDate = VNP_DATE_FORMAT.format(LocalDateTime.now());
        String expireDate = VNP_DATE_FORMAT.format(LocalDateTime.now().plusMinutes(15));

        long amountInCents = command.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", properties.getVersion());
        vnpParams.put("vnp_Command", properties.getCommand());
        vnpParams.put("vnp_TmnCode", properties.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amountInCents));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", command.getOrderCode());
        vnpParams.put("vnp_OrderInfo", command.getOrderInfo() != null ? command.getOrderInfo() : "Thanh toan don hang " + command.getOrderCode());
        vnpParams.put("vnp_OrderType", properties.getOrderType());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", command.getReturnUrl() != null ? command.getReturnUrl() : properties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", command.getClientIp() != null ? command.getClientIp() : "127.0.0.1");
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedKey = encode(fieldName);
                String encodedVal = encode(fieldValue);

                hashData.append(fieldName).append('=').append(encodedVal);
                query.append(encodedKey).append('=').append(encodedVal);

                if (i < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String secureHash = hmacSHA512(properties.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + secureHash;
        String paymentUrl = properties.getPayUrl() + "?" + queryUrl;

        String transactionCode = "VNP-" + command.getOrderCode() + "-" + createDate;

        return PaymentInitResult.builder()
                .paymentUrl(paymentUrl)
                .transactionCode(transactionCode)
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentProvider("VNPAY")
                .amount(command.getAmount())
                .build();
    }

    @Override
    public PaymentWebhookResult processWebhook(PaymentWebhookPayload payload) {
        Map<String, String> params = payload.getParams();
        if (params == null || params.isEmpty()) {
            return PaymentWebhookResult.builder()
                    .signatureValid(false)
                    .eventStatus(PaymentEventStatus.INVALID_SIGNATURE)
                    .responseCode("99")
                    .message("No parameters received")
                    .build();
        }

        String receivedSecureHash = params.get("vnp_SecureHash");
        if (receivedSecureHash == null || receivedSecureHash.isBlank()) {
            return PaymentWebhookResult.builder()
                    .signatureValid(false)
                    .eventStatus(PaymentEventStatus.INVALID_SIGNATURE)
                    .responseCode("97")
                    .message("Missing vnp_SecureHash")
                    .build();
        }

        boolean isValid = verifySignature(params, receivedSecureHash, properties.getHashSecret());
        if (!isValid) {
            log.warn("VNPay signature verification failed for order {}", params.get("vnp_TxnRef"));
            return PaymentWebhookResult.builder()
                    .signatureValid(false)
                    .eventStatus(PaymentEventStatus.INVALID_SIGNATURE)
                    .orderCode(params.get("vnp_TxnRef"))
                    .responseCode("97")
                    .message("Invalid Checksum")
                    .rawResponse(params.toString())
                    .build();
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");

        BigDecimal amount = BigDecimal.ZERO;
        if (amountStr != null && !amountStr.isBlank()) {
            try {
                amount = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100));
            } catch (Exception ex) {
                log.warn("Invalid amount format in VNPay webhook: {}", amountStr);
            }
        }

        PaymentEventStatus eventStatus;
        if ("00".equals(responseCode)) {
            eventStatus = PaymentEventStatus.SUCCESS;
        } else if ("24".equals(responseCode)) {
            eventStatus = PaymentEventStatus.CANCELLED;
        } else if ("11".equals(responseCode)) {
            eventStatus = PaymentEventStatus.EXPIRED;
        } else {
            eventStatus = PaymentEventStatus.FAILED;
        }

        String effectiveTxnCode = (transactionNo != null && !transactionNo.isBlank() && !"0".equals(transactionNo))
                ? "VNP-TXN-" + transactionNo
                : "VNP-" + txnRef + "-" + System.currentTimeMillis();

        return PaymentWebhookResult.builder()
                .signatureValid(true)
                .eventStatus(eventStatus)
                .orderCode(txnRef)
                .transactionCode(effectiveTxnCode)
                .amount(amount)
                .responseCode(responseCode)
                .message("00".equals(responseCode) ? "Confirm Success" : "Payment status: " + responseCode)
                .rawResponse(params.toString())
                .build();
    }

    public boolean verifySignature(Map<String, String> fields, String receivedHash, String secretKey) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashDataEncoded = new StringBuilder();
        StringBuilder hashDataRaw = new StringBuilder();

        boolean first = true;
        for (String fieldName : fieldNames) {
            if ("vnp_SecureHash".equals(fieldName) || "vnp_SecureHashType".equals(fieldName)) {
                continue;
            }
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (!first) {
                    hashDataEncoded.append('&');
                    hashDataRaw.append('&');
                }
                hashDataEncoded.append(fieldName).append('=').append(encode(fieldValue));
                hashDataRaw.append(fieldName).append('=').append(fieldValue);
                first = false;
            }
        }

        String calculatedHashEncoded = hmacSHA512(secretKey, hashDataEncoded.toString());
        String calculatedHashRaw = hmacSHA512(secretKey, hashDataRaw.toString());

        // Constant-time comparison
        return constantTimeEquals(calculatedHashEncoded, receivedHash) ||
               constantTimeEquals(calculatedHashRaw, receivedHash);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(
                a.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8),
                b.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmacSha512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmacSha512.init(secretKey);
            byte[] hash = hmacSha512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                result.append(String.format("%02x", b & 0xff));
            }
            return result.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA512", ex);
        }
    }

    private String encode(String val) {
        try {
            return URLEncoder.encode(val, StandardCharsets.US_ASCII.toString());
        } catch (Exception ex) {
            return val;
        }
    }
}
