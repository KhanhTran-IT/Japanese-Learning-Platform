package com.japaneselearning.module_quiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizSubmitAnswerReq {

    @NotNull(message = "questionId không được để trống")
    private Long questionId;

    /** For SINGLE_CHOICE / TRUE_FALSE / MULTIPLE_CHOICE */
    private Long answerId;

    /** For FILL_BLANK or text-based answers */
    private String userAnswerText;
}
