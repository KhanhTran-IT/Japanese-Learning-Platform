package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QuizResultAnswerRes {
    private Long questionId;
    private String questionContent;
    private String questionType;
    private Long selectedAnswerId;
    private String selectedAnswerContent;
    private Long correctAnswerId;
    private String correctAnswerContent;
    private String userAnswerText;
    private Boolean isCorrect;
    private BigDecimal pointsEarned;
    private String explanation;
}
