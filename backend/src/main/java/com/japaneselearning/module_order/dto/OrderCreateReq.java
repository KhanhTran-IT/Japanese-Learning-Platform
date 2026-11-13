package com.japaneselearning.module_order.dto;

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
public class OrderCreateReq {

    @NotNull(message = "Mã khóa học không được để trống")
    private Long courseId;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String note;

    @Size(max = 255, message = "Khóa idempotency không được vượt quá 255 ký tự")
    private String idempotencyKey;
}
