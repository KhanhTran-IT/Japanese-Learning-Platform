package com.japaneselearning.module_leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardRes {
    private LeaderboardUserRes currentUserRanking;
    private List<LeaderboardUserRes> rankings;
}
