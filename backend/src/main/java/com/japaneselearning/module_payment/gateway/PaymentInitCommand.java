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
public class PaymentInitCommand {
    private Long orderId;
    private String orderCode;
    private BigDecimal amount;
    private String orderInfo;
    private String clientIp;
    private String returnUrl;
}
