package com.japaneselearning.module_payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookResult {
    private boolean signatureValid;
    private PaymentEventStatus eventStatus;
    private String orderCode;
    private String transactionCode;
    private BigDecimal amount;
    private String responseCode;
    private String message;
    private String rawResponse;
}
