package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizRes {
    private Long id;
    private Long courseId;
    private Long lessonId;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private BigDecimal passingScore;
    private Integer maxAttempts;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
