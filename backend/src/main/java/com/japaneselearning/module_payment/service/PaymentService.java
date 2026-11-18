package com.japaneselearning.module_payment.service;

import com.japaneselearning.module_payment.dto.PaymentCreateReq;
import com.japaneselearning.module_payment.dto.PaymentInitRes;
import com.japaneselearning.module_payment.dto.PaymentRes;
import com.japaneselearning.module_payment.dto.VnPayIpnResponse;
import com.japaneselearning.module_payment.enums.PaymentMethod;

import java.util.Map;

public interface PaymentService {

    PaymentInitRes createPayment(PaymentCreateReq req);

    VnPayIpnResponse handleVnPayIpn(Map<String, String> allParams);

    PaymentRes handleGenericWebhook(PaymentMethod method, Map<String, String> params, String rawBody, String signature);

    PaymentRes simulatePayment(Long orderId);

    /**
     * Purges raw response payloads of payments older than the retention threshold (in days),
     * replacing them with the retention marker while preserving all core financial records.
     *
     * @param retentionDays Number of days to retain raw payloads (defaults to 90 if <= 0)
     * @return Number of payments whose raw payload was purged
     */
    int purgeExpiredPaymentPayloads(int retentionDays);
}
