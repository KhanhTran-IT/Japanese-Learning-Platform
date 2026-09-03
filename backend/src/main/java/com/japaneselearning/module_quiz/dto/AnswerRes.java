package com.japaneselearning.module_quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnswerRes {
    private Long id;
    private Long questionId;
    private String content;
    private Boolean isCorrect;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
