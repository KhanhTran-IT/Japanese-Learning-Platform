package com.japaneselearning.module_quiz.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_quiz.dto.*;
import com.japaneselearning.module_quiz.service.QuizAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Quiz", description = "Admin Quiz Management APIs")
public class QuizAdminController {

    private final QuizAdminService quizAdminService;

    // ==========================================
    // QUIZ
    // ==========================================

    @PostMapping("/quizzes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Create quiz", description = "Create a new quiz linked to a course or lesson")
    public ApiResponse<QuizRes> createQuiz(@Valid @RequestBody QuizCreateReq req) {
        return ApiResponse.success("Tạo quiz thành công", quizAdminService.createQuiz(req));
    }

    @GetMapping("/quizzes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get quizzes", description = "Get paginated list of quizzes by courseId or lessonId")
    public ApiResponse<Page<QuizRes>> getQuizzes(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long lessonId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ApiResponse.success("Lấy danh sách quiz thành công", quizAdminService.getQuizzes(courseId, lessonId, pageable));
    }

    @GetMapping("/quizzes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get quiz details", description = "Get details of a specific quiz")
    public ApiResponse<QuizRes> getQuiz(@PathVariable Long id) {
        return ApiResponse.success("Lấy thông tin quiz thành công", quizAdminService.getQuizById(id));
    }

    @PutMapping("/quizzes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Update quiz", description = "Update quiz details")
    public ApiResponse<QuizRes> updateQuiz(@PathVariable Long id, @Valid @RequestBody QuizUpdateReq req) {
        return ApiResponse.success("Cập nhật quiz thành công", quizAdminService.updateQuiz(id, req));
    }

    @DeleteMapping("/quizzes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete quiz", description = "Soft delete a quiz (sets status to ARCHIVED)")
    public ApiResponse<Void> deleteQuiz(@PathVariable Long id) {
        quizAdminService.deleteQuiz(id);
        return ApiResponse.success("Xóa quiz thành công", null);
    }

    @PutMapping("/quizzes/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Publish quiz", description = "Publish a quiz. It must have at least one question.")
    public ApiResponse<QuizRes> publishQuiz(@PathVariable Long id) {
        return ApiResponse.success("Xuất bản quiz thành công", quizAdminService.publishQuiz(id));
    }

    @PutMapping("/quizzes/{id}/hide")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Hide quiz", description = "Hide a quiz (sets status to HIDDEN)")
    public ApiResponse<QuizRes> hideQuiz(@PathVariable Long id) {
        return ApiResponse.success("Ẩn quiz thành công", quizAdminService.hideQuiz(id));
    }

    // ==========================================
    // QUESTION
    // ==========================================

    @PostMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Create question", description = "Add a new question to a quiz")
    public ApiResponse<QuestionRes> createQuestion(@PathVariable Long quizId, @Valid @RequestBody QuestionCreateReq req) {
        return ApiResponse.success("Thêm câu hỏi thành công", quizAdminService.createQuestion(quizId, req));
    }

    @GetMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get questions", description = "Get all questions for a quiz")
    public ApiResponse<List<QuestionRes>> getQuestions(@PathVariable Long quizId) {
        return ApiResponse.success("Lấy danh sách câu hỏi thành công", quizAdminService.getQuestionsByQuizId(quizId));
    }

    @GetMapping("/questions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get question details", description = "Get details of a specific question")
    public ApiResponse<QuestionRes> getQuestion(@PathVariable Long id) {
        return ApiResponse.success("Lấy thông tin câu hỏi thành công", quizAdminService.getQuestionById(id));
    }

    @PutMapping("/questions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Update question", description = "Update question details")
    public ApiResponse<QuestionRes> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionUpdateReq req) {
        return ApiResponse.success("Cập nhật câu hỏi thành công", quizAdminService.updateQuestion(id, req));
    }

    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete question", description = "Delete a question and its answers")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        quizAdminService.deleteQuestion(id);
        return ApiResponse.success("Xóa câu hỏi thành công", null);
    }

    // ==========================================
    // ANSWER
    // ==========================================

    @PostMapping("/questions/{questionId}/answers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Create answer", description = "Add a new answer to a question")
    public ApiResponse<AnswerRes> createAnswer(@PathVariable Long questionId, @Valid @RequestBody AnswerCreateReq req) {
        return ApiResponse.success("Thêm đáp án thành công", quizAdminService.createAnswer(questionId, req));
    }

    @GetMapping("/questions/{questionId}/answers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get answers", description = "Get all answers for a question")
    public ApiResponse<List<AnswerRes>> getAnswers(@PathVariable Long questionId) {
        return ApiResponse.success("Lấy danh sách đáp án thành công", quizAdminService.getAnswersByQuestionId(questionId));
    }

    @PutMapping("/answers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Update answer", description = "Update answer details")
    public ApiResponse<AnswerRes> updateAnswer(@PathVariable Long id, @Valid @RequestBody AnswerUpdateReq req) {
        return ApiResponse.success("Cập nhật đáp án thành công", quizAdminService.updateAnswer(id, req));
    }

    @DeleteMapping("/answers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete answer", description = "Delete an answer")
    public ApiResponse<Void> deleteAnswer(@PathVariable Long id) {
        quizAdminService.deleteAnswer(id);
        return ApiResponse.success("Xóa đáp án thành công", null);
    }
}
