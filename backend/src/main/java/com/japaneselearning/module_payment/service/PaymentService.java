package com.japaneselearning.module_payment.service;

import com.japaneselearning.module_payment.dto.PaymentRes;

public interface PaymentService {
    PaymentRes simulatePayment(Long orderId);
}
