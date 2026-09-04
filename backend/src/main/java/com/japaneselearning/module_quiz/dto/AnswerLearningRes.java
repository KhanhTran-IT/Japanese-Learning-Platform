package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Answer option shown to student BEFORE submit.
 * isCorrect is intentionally omitted to prevent cheating.
 */
@Data
@Builder
public class AnswerLearningRes {
    private Long id;
    private String content;
    private Integer sortOrder;
}
