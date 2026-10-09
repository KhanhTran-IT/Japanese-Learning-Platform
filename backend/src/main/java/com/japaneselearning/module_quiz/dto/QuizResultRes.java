package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuizResultRes {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private BigDecimal score;
    private BigDecimal passingScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Boolean passed;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private List<QuizResultAnswerRes> answers;
}
