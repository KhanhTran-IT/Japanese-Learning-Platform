package com.japaneselearning.module_payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookPayload {
    private Map<String, String> params;
    private String rawBody;
    private String signatureHeader;
    private String clientIp;
}
