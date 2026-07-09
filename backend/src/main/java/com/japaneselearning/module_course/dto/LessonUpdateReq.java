package com.japaneselearning.module_course.dto;

import com.japaneselearning.module_course.enums.CourseStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonUpdateReq {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Slug cannot exceed 255 characters")
    private String slug;

    private String content;

    private String videoUrl;

    @NotNull(message = "Preview status is required")
    private Boolean isPreview;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;

    @NotNull(message = "Status is required")
    private CourseStatus status;

    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationMinutes;
}
