package com.japaneselearning.module_quiz.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuizCreateReq {

    private Long courseId;
    
    private Long lessonId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    @Min(value = 0, message = "Thời gian làm bài không được âm")
    private Integer timeLimitMinutes;

    @Min(value = 0, message = "Điểm qua môn không được âm")
    private BigDecimal passingScore;

    @Min(value = 1, message = "Số lần làm tối đa phải lớn hơn 0")
    private Integer maxAttempts;
}
