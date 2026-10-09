package com.japaneselearning.module_course.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_course.dto.ResourceCreateReq;
import com.japaneselearning.module_course.dto.ResourceRes;
import com.japaneselearning.module_course.dto.ResourceUpdateReq;
import com.japaneselearning.module_course.service.LessonResourceAdminService;
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
@Tag(name = "Admin Lesson Resource", description = "Admin APIs for managing lesson resources")
public class LessonResourceAdminController {

    private final LessonResourceAdminService resourceService;

    @PostMapping("/lessons/{lessonId}/resources")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Create a resource for a lesson", description = "Create a new resource. Teacher must own the course.")
    public ApiResponse<ResourceRes> createResource(@PathVariable Long lessonId, @Valid @RequestBody ResourceCreateReq req) {
        return ApiResponse.success("Tạo tài liệu bài học thành công", resourceService.createResource(lessonId, req));
    }

    @GetMapping("/lessons/{lessonId}/resources")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get resources of a lesson", description = "Get list of resources ordered by sortOrder.")
    public ApiResponse<List<ResourceRes>> getResourcesByLessonId(@PathVariable Long lessonId) {
        return ApiResponse.success("Lấy danh sách tài liệu bài học thành công", resourceService.getResourcesByLessonId(lessonId));
    }

    @GetMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get a lesson resource", description = "Get resource details.")
    public ApiResponse<ResourceRes> getResource(@PathVariable Long id) {
        return ApiResponse.success("Lấy chi tiết tài liệu thành công", resourceService.getResource(id));
    }

    @PutMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Update lesson resource", description = "Update resource details.")
    public ApiResponse<ResourceRes> updateResource(@PathVariable Long id, @Valid @RequestBody ResourceUpdateReq req) {
        return ApiResponse.success("Cập nhật tài liệu thành công", resourceService.updateResource(id, req));
    }

    @DeleteMapping("/resources/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete lesson resource", description = "Delete a resource.")
    public ApiResponse<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ApiResponse.success("Xóa tài liệu thành công", null);
    }
}
