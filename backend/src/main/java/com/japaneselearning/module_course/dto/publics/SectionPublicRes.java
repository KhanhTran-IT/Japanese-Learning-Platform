package com.japaneselearning.module_course.dto.publics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionPublicRes {
    private Long id;
    private String title;
    private String description;
    private Integer sortOrder;
    private List<LessonPublicRes> lessons;
}
