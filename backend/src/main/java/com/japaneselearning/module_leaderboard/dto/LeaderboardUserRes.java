package com.japaneselearning.module_leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardUserRes {
    private Long rank;
    private String name;
    private String avatar;
    private Integer xp;
    private Integer level;
    private Integer streak;
    private String badge;
    private String country;
    private Boolean isMe;
}
