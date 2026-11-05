package com.japaneselearning.module_payment.gateway.simulation;

import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.gateway.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimulationPaymentGateway implements PaymentGateway {

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.SIMULATION;
    }

    @Override
    public PaymentInitResult initPayment(PaymentInitCommand command) {
        String transactionCode = "SIM-" + command.getOrderCode() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        String returnUrl = command.getReturnUrl() != null
                ? command.getReturnUrl()
                : "/student/checkout?orderId=" + command.getOrderId();

        return PaymentInitResult.builder()
                .paymentUrl(returnUrl)
                .transactionCode(transactionCode)
                .paymentMethod(PaymentMethod.SIMULATION)
                .paymentProvider("LOCAL_SIMULATION")
                .amount(command.getAmount())
                .build();
    }

    @Override
    public PaymentWebhookResult processWebhook(PaymentWebhookPayload payload) {
        String orderCode = payload.getParams() != null ? payload.getParams().get("orderCode") : null;
        String transactionCode = "SIM-WEBHOOK-" + (orderCode != null ? orderCode : UUID.randomUUID().toString().substring(0, 8));

        return PaymentWebhookResult.builder()
                .signatureValid(true)
                .eventStatus(PaymentEventStatus.SUCCESS)
                .orderCode(orderCode)
                .transactionCode(transactionCode)
                .responseCode("00")
                .message("Simulation webhook confirmed")
                .rawResponse("SIMULATION")
                .build();
    }
}
