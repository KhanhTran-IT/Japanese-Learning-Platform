package com.japaneselearning.module_quiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitAnswerReq {

    @NotNull(message = "questionId không được để trống")
    private Long questionId;

    /** For SINGLE_CHOICE / TRUE_FALSE / legacy single selection */
    private Long answerId;

    /** For MULTIPLE_CHOICE: list of selected answer IDs */
    private List<Long> answerIds;

    /** For FILL_BLANK or text-based answers */
    private String userAnswerText;

    /**
     * Resolves effective selected answer IDs from either answerIds or answerId.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<Long> getEffectiveAnswerIds() {
        if (answerIds != null && !answerIds.isEmpty()) {
            return answerIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
        if (answerId != null) {
            return List.of(answerId);
        }
        return List.of();
    }
}
