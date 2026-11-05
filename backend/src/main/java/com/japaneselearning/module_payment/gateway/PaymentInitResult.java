package com.japaneselearning.module_payment.gateway;

import com.japaneselearning.module_payment.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitResult {
    private String paymentUrl;
    private String transactionCode;
    private PaymentMethod paymentMethod;
    private String paymentProvider;
    private BigDecimal amount;
}
