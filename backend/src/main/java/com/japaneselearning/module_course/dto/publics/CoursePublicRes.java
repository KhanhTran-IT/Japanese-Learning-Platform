package com.japaneselearning.module_course.dto.publics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePublicRes {
    private Long id;
    private String title;
    private String slug;
    private String shortDescription;
    private String thumbnailUrl;
    private String level;
    private String courseType;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Double averageRating;
    private Integer totalStudents;
    private String teacherName;
    private String teacherAvatarUrl;
    private Integer totalDurationMinutes;
    private Integer totalLessons;
}
