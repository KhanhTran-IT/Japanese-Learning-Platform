package com.japaneselearning.module_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningCurriculumRes {
    private Long courseId;
    private String courseTitle;
    private String courseSlug;
    
    private Long currentLessonId;
    private Long previousLessonId;
    private Long nextLessonId;
    
    private List<LearningSectionRes> sections;
}
