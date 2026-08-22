package com.japaneselearning.module_course.dto;

import com.japaneselearning.module_course.enums.ResourceType;
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
public class ResourceCreateReq {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotNull(message = "Resource type is required")
    private ResourceType resourceType;

    @NotBlank(message = "File URL is required")
    @Size(max = 1000, message = "File URL cannot exceed 1000 characters")
    private String fileUrl;

    @Min(value = 0, message = "File size cannot be negative")
    private Long fileSize;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;
}
