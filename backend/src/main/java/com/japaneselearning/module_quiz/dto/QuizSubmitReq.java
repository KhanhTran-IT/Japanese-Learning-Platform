package com.japaneselearning.module_quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuizSubmitReq {

    @NotNull(message = "attemptId không được để trống")
    private Long attemptId;

    @NotEmpty(message = "Danh sách câu trả lời không được rỗng")
    @Valid
    private List<QuizSubmitAnswerReq> answers;
}
