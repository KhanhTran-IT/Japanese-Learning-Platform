package com.japaneselearning.module_quiz.dto;

import com.japaneselearning.module_quiz.enums.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuestionCreateReq {

    @NotNull(message = "Loại câu hỏi không được để trống")
    private QuestionType questionType;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    private String audioUrl;
    
    private String imageUrl;
    
    private String explanation;

    @Min(value = 0, message = "Điểm số không được âm")
    private BigDecimal points;

    @Min(value = 0, message = "Thứ tự không được âm")
    private Integer sortOrder;
}
