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
}
