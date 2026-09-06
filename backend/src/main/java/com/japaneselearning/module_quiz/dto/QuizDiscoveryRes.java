package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QuizDiscoveryRes {
    private Long id;
    private Long courseId;
    private Long lessonId;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private BigDecimal passingScore;
    private Integer maxAttempts;
    private Integer questionCount;
    private Long latestAttemptId;
    private String latestAttemptStatus;
    private BigDecimal latestScore;
    private Boolean latestPassed;
    private Long remainingAttempts;
}
