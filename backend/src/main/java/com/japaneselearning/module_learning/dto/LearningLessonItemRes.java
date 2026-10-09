package com.japaneselearning.module_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningLessonItemRes {
    private Long id;
    private String title;
    private Integer sortOrder;
    private Integer durationMinutes;
    private Boolean isPreview;
    
    // Progress information
    private Boolean isCompleted;
    private Double watchedPercent;
}
