package com.japaneselearning.module_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonLearningRes {
    private Long id;
    private String title;
    private String slug;
    private String content;
    private String videoUrl;
    private Boolean isPreview;
    private Integer sortOrder;
    private Integer durationMinutes;
    
    // Progress information
    private Double watchedPercent;
    private Boolean isCompleted;
}
