package com.japaneselearning.module_learning.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_learning.dto.LessonLearningRes;
import com.japaneselearning.module_learning.dto.ProgressUpdateReq;
import com.japaneselearning.module_learning.service.LearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@Tag(name = "Lesson Learning & Progress", description = "APIs for student to learn lesson and track progress")
public class LearningController {

    private final LearningService learningService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get lesson details for learning", description = "Returns full lesson details including progress. Checks enrollment if lesson is not preview.")
    public ApiResponse<LessonLearningRes> getLessonDetail(@PathVariable Long id) {
        return ApiResponse.success("Lấy thông tin bài học thành công", learningService.getLessonDetail(id));
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update lesson progress", description = "Upserts lesson progress. watchedPercent is only updated if the new value is higher.")
    public ApiResponse<Void> updateProgress(@PathVariable Long id, @Valid @RequestBody ProgressUpdateReq req) {
        learningService.updateProgress(id, req);
        return ApiResponse.success("Cập nhật tiến độ thành công", null);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Complete a lesson", description = "Marks a lesson as fully completed and watched 100%. Idempotent.")
    public ApiResponse<Void> completeLesson(@PathVariable Long id) {
        learningService.completeLesson(id);
        return ApiResponse.success("Hoàn thành bài học thành công", null);
    }
}
