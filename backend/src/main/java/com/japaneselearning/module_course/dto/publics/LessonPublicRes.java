package com.japaneselearning.module_course.dto.publics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonPublicRes {
    private Long id;
    private String title;
    private String slug;
    private String content; // Sẽ gán null nếu không học thử
    private String videoUrl; // Sẽ gán null nếu không học thử
    private Boolean isPreview;
    private Integer sortOrder;
    private Integer durationMinutes;
}
