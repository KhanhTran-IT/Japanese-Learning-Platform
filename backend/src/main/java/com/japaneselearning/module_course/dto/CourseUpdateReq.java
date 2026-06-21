package com.japaneselearning.module_course.dto;

import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateReq {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Slug cannot exceed 255 characters")
    private String slug;

    private String shortDescription;

    private String description;

    private String thumbnailUrl;

    @NotNull(message = "Course level is required")
    private CourseLevel level;

    @NotNull(message = "Course type is required")
    private CourseType courseType;

    @Min(value = 0, message = "Original price cannot be negative")
    private BigDecimal originalPrice;

    @Min(value = 0, message = "Sale price cannot be negative")
    private BigDecimal salePrice;

    @NotNull(message = "Course status is required")
    private CourseStatus status;
}
