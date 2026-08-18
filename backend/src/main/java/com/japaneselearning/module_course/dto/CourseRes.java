package com.japaneselearning.module_course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRes {
    private Long id;
    
    // Teacher Info flattened
    private Long teacherId;
    private String teacherName;
    private String teacherAvatarUrl;
    
    private String title;
    private String slug;
    private String shortDescription;
    private String description;
    private String thumbnailUrl;
    private String level;
    private String courseType;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private String status;
    private Integer totalDurationMinutes;
    private Integer totalLessons;
    private Double averageRating;
    private Integer totalStudents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
