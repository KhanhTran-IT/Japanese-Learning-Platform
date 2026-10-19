package com.japaneselearning.module_payment.dto;

import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRes {
    private Long id;
    private Long orderId;
    private String orderCode;
    private PaymentMethod paymentMethod;
    private String paymentProvider;
    private String transactionCode;
    private BigDecimal amount;
    private PaymentStatus status;
    private OrderStatus orderStatus;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
