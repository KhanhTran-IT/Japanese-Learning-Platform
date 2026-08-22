package com.japaneselearning.module_course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRes {
    private Long id;
    private Long lessonId;
    private String title;
    private String resourceType;
    private String fileUrl;
    private Long fileSize;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
