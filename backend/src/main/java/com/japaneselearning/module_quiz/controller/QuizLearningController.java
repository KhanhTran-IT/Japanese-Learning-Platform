package com.japaneselearning.module_quiz.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_quiz.dto.*;
import com.japaneselearning.module_quiz.service.QuizLearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Student Quiz", description = "Student Quiz Taking APIs")
public class QuizLearningController {

    private final QuizLearningService quizLearningService;

    @GetMapping("/api/v1/quizzes/{id}")
    @Operation(summary = "Get quiz detail for student",
               description = "Returns published quiz with questions and answer options (isCorrect hidden)")
    public ApiResponse<QuizLearningRes> getQuiz(@PathVariable Long id) {
        return ApiResponse.success("Lấy thông tin quiz thành công", quizLearningService.getQuizForStudent(id));
    }

    @PostMapping("/api/v1/quizzes/{id}/start")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Start quiz attempt",
               description = "Creates a new attempt. Checks maxAttempts limit.")
    public ApiResponse<QuizAttemptStartRes> startAttempt(@PathVariable Long id) {
        return ApiResponse.success("Bắt đầu làm bài thành công", quizLearningService.startAttempt(id));
    }

    @PostMapping("/api/v1/quizzes/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit quiz attempt",
               description = "Submit answers, auto-score for SINGLE_CHOICE/TRUE_FALSE, and return result")
    public ApiResponse<QuizResultRes> submitAttempt(
            @PathVariable Long id,
            @Valid @RequestBody QuizSubmitReq req) {
        return ApiResponse.success("Nộp bài thành công", quizLearningService.submitAttempt(id, req));
    }

    @GetMapping("/api/v1/quizzes/{id}/result/{attemptId}")
    @Operation(summary = "Get quiz attempt result",
               description = "Returns detailed result. Only attempt owner or admin/super admin can view.")
    public ApiResponse<QuizResultRes> getResult(@PathVariable Long id, @PathVariable Long attemptId) {
        return ApiResponse.success("Lấy kết quả bài làm thành công", quizLearningService.getAttemptResult(id, attemptId));
    }

    @GetMapping("/api/v1/lessons/{lessonId}/quizzes")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get quizzes for lesson",
               description = "Returns published quizzes associated with a lesson that the student is allowed to see")
    public ApiResponse<List<QuizDiscoveryRes>> getLessonQuizzes(@PathVariable Long lessonId) {
        return ApiResponse.success("Lấy danh sách quiz thành công", quizLearningService.getLessonQuizzes(lessonId));
    }

    @GetMapping("/api/users/me/quiz-attempts")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my quiz attempts",
               description = "Returns all quiz attempts of the current student, sorted by most recent")
    public ApiResponse<List<QuizAttemptSummaryRes>> getMyAttempts() {
        return ApiResponse.success("Lấy lịch sử làm bài thành công", quizLearningService.getMyAttempts());
    }
}
