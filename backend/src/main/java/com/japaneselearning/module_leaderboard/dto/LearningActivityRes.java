package com.japaneselearning.module_leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningActivityRes {

    private Long id;
    private String activityType;
    private Integer xpAmount;
    private Long referenceId;
    private String description;
    private LocalDateTime createdAt;
}
