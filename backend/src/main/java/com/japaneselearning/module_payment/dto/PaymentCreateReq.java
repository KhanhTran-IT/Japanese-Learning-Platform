package com.japaneselearning.module_payment.dto;

import com.japaneselearning.module_payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateReq {

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Long orderId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    @Size(max = 2048, message = "Đường dẫn trả về không được vượt quá 2048 ký tự")
    private String returnUrl;
}
