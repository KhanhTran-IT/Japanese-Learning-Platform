package com.japaneselearning.module_leaderboard.policy;

import java.time.Duration;

/**
 * Retention policy for learning activity history events.
 *
 * Requirements & Invariants:
 * 1. Active History Retention Window: Granular learning activity events
 *    (LESSON_COMPLETED, QUIZ_PASSED, FLASHCARD_REVIEWED, DAILY_STREAK)
 *    are actively retained for 180 days (6 months) for student review and audit purposes.
 * 2. Cumulative Gamification Totals Invariant: Cumulative user XP scores
 *    (in table {@code user_scores} for ALL_TIME, WEEKLY, MONTHLY periods) and user level
 *    ({@code users.level}) are permanent cumulative ledgers. Purging or archiving
 *    historical activity detail events older than the retention threshold MUST NEVER
 *    reduce or alter user accumulated XP, level, or streak.
 * 3. Bounded & Safe Execution: Purge jobs must operate within bounded transaction limits
 *    to prevent database lock escalation and long-running table locks.
 */
public final class LearningActivityRetentionPolicy {

    /**
     * Standard retention duration for granular learning activity history: 180 days.
     */
    public static final Duration ACTIVITY_RETENTION_DURATION = Duration.ofDays(180);

    /**
     * Default number of days to retain learning activities before archival / purging.
     */
    public static final int DEFAULT_RETENTION_DAYS = 180;

    /**
     * Minimum allowable retention days to prevent accidental deletion of recent records.
     */
    public static final int MIN_RETENTION_DAYS = 30;

    /**
     * Maximum batch size per purge iteration to keep lock duration minimal.
     */
    public static final int PURGE_BATCH_SIZE = 1000;

    private LearningActivityRetentionPolicy() {
        // Prevent instantiation of utility constants class
    }
}
