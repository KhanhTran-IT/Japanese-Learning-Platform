package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class QuizLearningRes {
    private Long id;
    private Long courseId;
    private Long lessonId;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private BigDecimal passingScore;
    private Integer maxAttempts;
    private List<QuestionLearningRes> questions;
}
