package com.japaneselearning.module_order.dto;

import jakarta.validation.constraints.NotNull;
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

    private String note;
}
