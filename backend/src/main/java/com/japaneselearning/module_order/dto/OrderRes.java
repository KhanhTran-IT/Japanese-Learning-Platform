package com.japaneselearning.module_order.dto;

import com.japaneselearning.module_order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRes {
    private Long id;
    private String orderCode;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private OrderStatus status;
    private String couponCode;
    private String note;
    private List<OrderItemRes> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
