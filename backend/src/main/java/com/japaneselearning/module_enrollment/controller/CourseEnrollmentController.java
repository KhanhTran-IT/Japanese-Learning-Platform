package com.japaneselearning.module_enrollment.controller;

import com.japaneselearning.common.response.ApiResponse;
import com.japaneselearning.module_enrollment.service.CourseEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Enrollment", description = "APIs for course enrollment")
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;

    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Enroll in a free course", description = "Allows a student to enroll in a free and published course")
    public ApiResponse<Void> enrollFreeCourse(@PathVariable Long courseId) {
        enrollmentService.enrollFreeCourse(courseId);
        return ApiResponse.success("Ghi danh thành công", null);
    }
}
