package com.japaneselearning.module_quiz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerUpdateReq {

    @NotBlank(message = "Nội dung đáp án không được để trống")
    private String content;

    @NotNull(message = "Phải xác định đáp án này là đúng hay sai")
    private Boolean isCorrect;

    @Min(value = 0, message = "Thứ tự không được âm")
    private Integer sortOrder;
}
