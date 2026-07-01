package com.japaneselearning.module_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyProgressOverviewRes {
    private Long totalEnrolledCourses;
    private Long totalCompletedLessons;
    private Double overallProgressPercent;
}
