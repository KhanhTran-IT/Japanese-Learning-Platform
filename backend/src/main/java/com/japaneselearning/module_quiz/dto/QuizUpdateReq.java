package com.japaneselearning.module_quiz.dto;

import com.japaneselearning.module_quiz.enums.QuizStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuizUpdateReq {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    @Min(value = 0, message = "Thời gian làm bài không được âm")
    private Integer timeLimitMinutes;

    @Min(value = 0, message = "Điểm qua môn không được âm")
    private BigDecimal passingScore;

    @Min(value = 1, message = "Số lần làm tối đa phải lớn hơn 0")
    private Integer maxAttempts;

    @NotNull(message = "Trạng thái không được để trống")
    private QuizStatus status;
}
