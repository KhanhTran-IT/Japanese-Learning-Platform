package com.japaneselearning.module_course.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_course.dto.publics.CourseDetailPublicRes;
import com.japaneselearning.module_course.dto.publics.CoursePublicRes;
import com.japaneselearning.module_course.service.CoursePublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Public Course", description = "Public Course APIs for Student and Guest")
public class CoursePublicController {

    private final CoursePublicService coursePublicService;

    @GetMapping
    @Operation(summary = "Get published courses", description = "Get a paginated list of all published courses.")
    public ApiResponse<Page<CoursePublicRes>> getPublishedCourses(
            @RequestParam(required = false) String level,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ApiResponse.success("Lấy danh sách khóa học thành công", coursePublicService.getPublishedCourses(level, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get course details by slug", description = "Get full course details including sections and lessons. Hides sensitive lesson data if not previewable.")
    public ApiResponse<CourseDetailPublicRes> getCourseDetailBySlug(@PathVariable String slug) {
        return ApiResponse.success("Lấy chi tiết khóa học thành công", coursePublicService.getCourseDetailBySlug(slug));
    }
}
