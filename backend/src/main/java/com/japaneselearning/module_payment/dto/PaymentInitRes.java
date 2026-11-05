package com.japaneselearning.module_payment.dto;

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
public class PaymentInitRes {
    private Long orderId;
    private String orderCode;
    private String paymentUrl;
    private String transactionCode;
    private PaymentMethod paymentMethod;
    private String paymentProvider;
    private BigDecimal amount;
}
