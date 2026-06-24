package com.japaneselearning.module_course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateReq {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Slug cannot exceed 255 characters")
    private String slug;

    private String content;

    private String videoUrl;

    @Builder.Default
    private Boolean isPreview = false;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;
}
