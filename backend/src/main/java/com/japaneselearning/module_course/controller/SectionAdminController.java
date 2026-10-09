package com.japaneselearning.module_course.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_course.dto.SectionCreateReq;
import com.japaneselearning.module_course.dto.SectionRes;
import com.japaneselearning.module_course.dto.SectionUpdateReq;
import com.japaneselearning.module_course.service.SectionAdminService;
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
@Tag(name = "Admin Course Section", description = "Admin Course Section Management APIs")
public class SectionAdminController {

    private final SectionAdminService sectionAdminService;

    @PostMapping("/courses/{courseId}/sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Create a new section for a course", description = "Create a section. Teacher must own the course.")
    public ApiResponse<SectionRes> createSection(@PathVariable Long courseId, @Valid @RequestBody SectionCreateReq req) {
        return ApiResponse.success("Tạo chương học thành công", sectionAdminService.createSection(courseId, req));
    }

    @GetMapping("/courses/{courseId}/sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get sections of a course", description = "Get list of sections ordered by sortOrder.")
    public ApiResponse<List<SectionRes>> getSectionsByCourseId(@PathVariable Long courseId) {
        return ApiResponse.success("Lấy danh sách chương học thành công", sectionAdminService.getSectionsByCourseId(courseId));
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Update section", description = "Update section details.")
    public ApiResponse<SectionRes> updateSection(@PathVariable Long id, @Valid @RequestBody SectionUpdateReq req) {
        return ApiResponse.success("Cập nhật chương học thành công", sectionAdminService.updateSection(id, req));
    }

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete section", description = "Delete a section. Cannot delete if it contains lessons.")
    public ApiResponse<Void> deleteSection(@PathVariable Long id) {
        sectionAdminService.deleteSection(id);
        return ApiResponse.success("Xóa chương học thành công", null);
    }
}
