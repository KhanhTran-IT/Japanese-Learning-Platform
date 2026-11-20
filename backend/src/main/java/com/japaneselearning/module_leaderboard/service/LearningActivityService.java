package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_leaderboard.dto.LearningActivityRes;
import com.japaneselearning.module_leaderboard.policy.LearningActivityRetentionPolicy;
import org.springframework.data.domain.Pageable;

public interface LearningActivityService {

    /**
     * Get paginated activity history for the currently authenticated student.
     * Enforces deterministic sorting: createdAt DESC, id DESC.
     * Enforces strict user isolation: students can only view their own activities.
     *
     * @param pageable requested page and size
     * @return paginated learning activities
     */
    PageResponse<LearningActivityRes> getMyActivities(Pageable pageable);

    /**
     * Calculate or retrieve total XP for a user without loading complete activity history into memory.
     * Prioritizes persisted UserScore (ALL_TIME) or executes database aggregate SUM().
     *
     * @param userId user ID
     * @return total XP
     */
    Long getTotalXpForUser(Long userId);

    /**
     * Purge historical learning activities older than the retention threshold.
     * Preserves gamification invariants: user_scores, user levels, and streaks are 100% unaffected.
     *
     * @param retentionDays number of days to retain (must be >= MIN_RETENTION_DAYS)
     * @return number of deleted activity rows
     */
    int purgeExpiredActivities(int retentionDays);

    /**
     * Purge historical learning activities using default retention policy (180 days).
     *
     * @return number of deleted activity rows
     */
    default int purgeExpiredActivities() {
        return purgeExpiredActivities(LearningActivityRetentionPolicy.DEFAULT_RETENTION_DAYS);
    }
}
