package com.japaneselearning.module_quiz.dto;

import com.japaneselearning.module_quiz.enums.QuestionType;
import com.japaneselearning.common.validator.ValidMediaUrl;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuestionCreateReq {

    @NotNull(message = "Loại câu hỏi không được để trống")
    private QuestionType questionType;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(max = 5000, message = "Nội dung câu hỏi không được vượt quá 5000 ký tự")
    private String content;

    @Size(max = 2048, message = "Đường dẫn âm thanh không được vượt quá 2048 ký tự")
    @ValidMediaUrl
    private String audioUrl;
    
    @Size(max = 2048, message = "Đường dẫn hình ảnh không được vượt quá 2048 ký tự")
    @ValidMediaUrl
    private String imageUrl;
    
    @Size(max = 5000, message = "Giải thích không được vượt quá 5000 ký tự")
    private String explanation;

    @Min(value = 0, message = "Điểm số không được âm")
    private BigDecimal points;

    @Min(value = 0, message = "Thứ tự không được âm")
    private Integer sortOrder;
}
