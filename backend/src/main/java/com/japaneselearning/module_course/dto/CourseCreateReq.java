package com.japaneselearning.module_course.dto;

import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.common.validator.ValidMediaUrl;
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
public class CourseCreateReq {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Slug cannot exceed 255 characters")
    private String slug;

    @Size(max = 1000, message = "Mô tả ngắn không được vượt quá 1000 ký tự")
    private String shortDescription;

    @Size(max = 50000, message = "Mô tả chi tiết không được vượt quá 50,000 ký tự")
    private String description;

    @Size(max = 2048, message = "Đường dẫn hình thu nhỏ không được vượt quá 2048 ký tự")
    @ValidMediaUrl
    private String thumbnailUrl;

    @NotNull(message = "Course level is required")
    private CourseLevel level;

    @NotNull(message = "Course type is required")
    private CourseType courseType;

    @Min(value = 0, message = "Original price cannot be negative")
    private BigDecimal originalPrice;

    @Min(value = 0, message = "Sale price cannot be negative")
    private BigDecimal salePrice;
}
