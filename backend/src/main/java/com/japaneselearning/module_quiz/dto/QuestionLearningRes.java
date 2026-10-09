package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class QuestionLearningRes {
    private Long id;
    private String questionType;
    private String content;
    private String audioUrl;
    private String imageUrl;
    private BigDecimal points;
    private Integer sortOrder;
    private List<AnswerLearningRes> answers;
}
