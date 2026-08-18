package com.japaneselearning.module_course.dto.publics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailPublicRes {
    private Long id;
    private String title;
    private String slug;
    private String shortDescription;
    private String description;
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
    
    private List<SectionPublicRes> sections;
}
