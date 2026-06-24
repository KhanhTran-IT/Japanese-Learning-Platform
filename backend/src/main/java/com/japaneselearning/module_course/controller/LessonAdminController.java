package com.japaneselearning.module_course.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_course.dto.LessonCreateReq;
import com.japaneselearning.module_course.dto.LessonRes;
import com.japaneselearning.module_course.dto.LessonUpdateReq;
import com.japaneselearning.module_course.service.LessonAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Lesson", description = "Admin Lesson Management APIs")
public class LessonAdminController {

    private final LessonAdminService lessonAdminService;

    @PostMapping("/sections/{sectionId}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Create a new lesson for a section", description = "Create a lesson. Teacher must own the course.")
    public ApiResponse<LessonRes> createLesson(@PathVariable Long sectionId, @Valid @RequestBody LessonCreateReq req) {
        return ApiResponse.success("Tạo bài học thành công", lessonAdminService.createLesson(sectionId, req));
    }

    @GetMapping("/sections/{sectionId}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get lessons of a section", description = "Get list of lessons ordered by sortOrder.")
    public ApiResponse<List<LessonRes>> getLessonsBySectionId(@PathVariable Long sectionId) {
        return ApiResponse.success("Lấy danh sách bài học thành công", lessonAdminService.getLessonsBySectionId(sectionId));
    }

    @GetMapping("/lessons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get a lesson", description = "Get lesson details.")
    public ApiResponse<LessonRes> getLesson(@PathVariable Long id) {
        return ApiResponse.success("Lấy chi tiết bài học thành công", lessonAdminService.getLesson(id));
    }

    @PutMapping("/lessons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Update lesson", description = "Update lesson details.")
    public ApiResponse<LessonRes> updateLesson(@PathVariable Long id, @Valid @RequestBody LessonUpdateReq req) {
        return ApiResponse.success("Cập nhật bài học thành công", lessonAdminService.updateLesson(id, req));
    }

    @DeleteMapping("/lessons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete lesson", description = "Delete a lesson.")
    public ApiResponse<Void> deleteLesson(@PathVariable Long id) {
        lessonAdminService.deleteLesson(id);
        return ApiResponse.success("Xóa bài học thành công", null);
    }
}
