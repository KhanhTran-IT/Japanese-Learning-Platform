package com.japaneselearning.module_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCourseRes {
    private Long courseId;
    private String courseName;
    private String slug;
    private String thumbnailUrl;
    private Double progressPercent;
    private Integer completedLessons;
    private Integer totalLessons;
    private String lastLessonName;
    private String lastLessonSlug;
    private LocalDateTime enrolledAt;
}
