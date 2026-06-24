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
public class LessonRes {
    private Long id;
    private Long sectionId;
    private String title;
    private String slug;
    private String content;
    private String videoUrl;
    private Boolean isPreview;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
