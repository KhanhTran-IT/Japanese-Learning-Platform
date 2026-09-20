package com.japaneselearning.module_quiz.dto;

import com.japaneselearning.common.validator.ValidMediaUrl;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuestionUpdateReq {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    @ValidMediaUrl
    private String audioUrl;
    
    @ValidMediaUrl
    private String imageUrl;
    
    private String explanation;

    @Min(value = 0, message = "Điểm số không được âm")
    private BigDecimal points;

    @Min(value = 0, message = "Thứ tự không được âm")
    private Integer sortOrder;
}
