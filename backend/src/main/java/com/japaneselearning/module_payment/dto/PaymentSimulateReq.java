package com.japaneselearning.module_payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSimulateReq {

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Long orderId;
}
