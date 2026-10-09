package com.japaneselearning.module_admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentCourseRes {
    private Long id;
    private String title;
    private String teacherName;
    
    @JsonProperty("isPublished")
    private Boolean isPublished;
    
    private LocalDateTime createdAt;
}
