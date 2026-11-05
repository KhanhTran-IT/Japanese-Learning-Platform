package com.japaneselearning.module_payment.gateway;

import com.japaneselearning.module_payment.enums.PaymentMethod;

public interface PaymentGateway {

    PaymentMethod getSupportedMethod();

    PaymentInitResult initPayment(PaymentInitCommand command);

    PaymentWebhookResult processWebhook(PaymentWebhookPayload payload);
}
