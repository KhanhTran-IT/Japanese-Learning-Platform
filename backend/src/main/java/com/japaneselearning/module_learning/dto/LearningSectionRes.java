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
public class LearningSectionRes {
    private Long id;
    private String title;
    private Integer sortOrder;
    private List<LearningLessonItemRes> lessons;
}
