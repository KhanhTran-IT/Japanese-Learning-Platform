package com.japaneselearning.module_course.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_course.dto.CourseCreateReq;
import com.japaneselearning.module_course.dto.CourseRes;
import com.japaneselearning.module_course.dto.CourseUpdateReq;
import com.japaneselearning.module_course.service.CourseAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@Tag(name = "Admin Course", description = "Admin Course Management APIs")
public class CourseAdminController {

    private final CourseAdminService courseAdminService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Create a new course", description = "Create a course. Teacher info is extracted from token.")
    public ApiResponse<CourseRes> createCourse(@Valid @RequestBody CourseCreateReq req) {
        return ApiResponse.success("Tạo khóa học thành công", courseAdminService.createCourse(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Update course", description = "Update course details. Teacher can only update their own courses.")
    public ApiResponse<CourseRes> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseUpdateReq req) {
        return ApiResponse.success("Cập nhật khóa học thành công", courseAdminService.updateCourse(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete course", description = "Soft delete a course (set status to ARCHIVED).")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        courseAdminService.deleteCourse(id);
        return ApiResponse.success("Xóa khóa học thành công", null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get course details", description = "Get details of a specific course.")
    public ApiResponse<CourseRes> getCourse(@PathVariable Long id) {
        return ApiResponse.success("Lấy thông tin khóa học thành công", courseAdminService.getCourse(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TEACHER')")
    @Operation(summary = "Get all courses", description = "Get paginated list of courses.")
    public ApiResponse<Page<CourseRes>> getCourses(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ApiResponse.success("Lấy danh sách khóa học thành công", courseAdminService.getCourses(pageable));
    }
}
