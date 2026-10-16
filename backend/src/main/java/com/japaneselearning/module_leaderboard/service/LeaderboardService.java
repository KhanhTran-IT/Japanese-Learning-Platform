package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.module_leaderboard.dto.LeaderboardRes;

public interface LeaderboardService {
    LeaderboardRes getLeaderboard(String timeframe, int page, int size);
}
