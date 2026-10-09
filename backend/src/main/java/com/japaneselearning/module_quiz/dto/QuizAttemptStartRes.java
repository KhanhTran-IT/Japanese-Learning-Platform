package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuizAttemptStartRes {
    private Long attemptId;
    private Long quizId;
    private LocalDateTime startedAt;
    private String status;
    private Integer maxAttempts;
    private Long remainingAttempts;
}
