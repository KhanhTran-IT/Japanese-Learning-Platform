package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class QuestionRes {
    private Long id;
    private Long quizId;
    private String questionType;
    private String content;
    private String audioUrl;
    private String imageUrl;
    private String explanation;
    private BigDecimal points;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
