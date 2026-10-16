package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.module_leaderboard.entity.LearningActivity;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.repository.LearningActivityRepository;
import com.japaneselearning.module_leaderboard.repository.UserScoreRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final LearningActivityRepository activityRepository;
    private final UserScoreRepository scoreRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addXp(Long userId, String activityType, Integer xpAmount, Long referenceId, String description) {
        // Prevent cheat (idempotency check)
        if (referenceId != null) {
            if (activityRepository.findByUserIdAndActivityTypeAndReferenceId(userId, activityType, referenceId).isPresent()) {
                return; // Already rewarded for this reference (e.g. lesson completed)
            }
        }

        User user = userRepository.findById(userId).orElseThrow();

        // 1. Log activity
        LearningActivity activity = LearningActivity.builder()
                .user(user)
                .activityType(activityType)
                .xpAmount(xpAmount)
                .referenceId(referenceId)
                .description(description)
                .build();
        activityRepository.save(activity);

        // 2. Update scores (ALL_TIME, WEEKLY, MONTHLY)
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        String currentWeek = now.getYear() + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String currentMonth = now.getYear() + "-" + String.format("%02d", now.getMonthValue());

        updateUserScore(user, "ALL_TIME", "ALL", xpAmount);
        updateUserScore(user, "WEEKLY", currentWeek, xpAmount);
        updateUserScore(user, "MONTHLY", currentMonth, xpAmount);

        // 3. Update User Streak & Level (Simplified level up: 1 level per 1000 XP)
        if (user.getLastActivityDate() == null || user.getLastActivityDate().isBefore(now)) {
            if (user.getLastActivityDate() != null && user.getLastActivityDate().plusDays(1).equals(now)) {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
            } else if (user.getLastActivityDate() == null || user.getLastActivityDate().isBefore(now.minusDays(1))) {
                user.setCurrentStreak(1);
            }
            user.setLastActivityDate(now);
        }
        
        // Let's assume total XP determines level. For efficiency, we should fetch total XP from ALL_TIME score.
        UserScore allTimeScore = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(userId, "ALL_TIME", "ALL").orElse(null);
        if (allTimeScore != null) {
            int newLevel = (allTimeScore.getTotalXp() / 1000) + 1;
            user.setLevel(newLevel);
        }
        
        userRepository.save(user);
    }

    private void updateUserScore(User user, String periodType, String periodValue, Integer xp) {
        UserScore score = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(user.getId(), periodType, periodValue)
                .orElse(UserScore.builder()
                        .user(user)
                        .periodType(periodType)
                        .periodValue(periodValue)
                        .totalXp(0)
                        .build());
        score.setTotalXp(score.getTotalXp() + xp);
        scoreRepository.save(score);
    }
}
