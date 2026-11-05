package com.japaneselearning.module_payment.gateway.vnpay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.payment.vnpay")
@Data
public class VnPayProperties {
    private String tmnCode = "DEMOTMN1";
    private String hashSecret = "DEMOHASHSECRET1234567890ABCDEF";
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:5173/student/checkout/vnpay-return";
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";
}
