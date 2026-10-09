package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary of a quiz attempt for the /me/quiz-attempts listing.
 */
@Data
@Builder
public class QuizAttemptSummaryRes {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private BigDecimal score;
    private BigDecimal passingScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private Boolean passed;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
