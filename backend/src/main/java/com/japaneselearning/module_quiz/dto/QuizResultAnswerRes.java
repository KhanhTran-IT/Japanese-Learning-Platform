package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class QuizResultAnswerRes {
    private Long questionId;
    private String questionContent;
    private String questionType;
    private Long selectedAnswerId;
    private List<Long> selectedAnswerIds;
    private String selectedAnswerContent;
    private Long correctAnswerId;
    private List<Long> correctAnswerIds;
    private String correctAnswerContent;
    private String userAnswerText;
    private Boolean isCorrect;
    private BigDecimal pointsEarned;
    private String explanation;
}
