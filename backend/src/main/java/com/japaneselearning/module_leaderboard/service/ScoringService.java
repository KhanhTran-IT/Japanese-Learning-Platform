package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_leaderboard.entity.LearningActivity;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.repository.LearningActivityRepository;
import com.japaneselearning.module_leaderboard.repository.UserScoreRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final LearningActivityRepository activityRepository;
    private final UserScoreRepository scoreRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addXp(Long userId, String activityType, Integer xpAmount, Long referenceId, String description) {
        if (userId == null || activityType == null || xpAmount == null || xpAmount <= 0) {
            return;
        }

        // 1. Serialize all scoring/gamification updates for this user via pessimistic write lock on User
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng: " + userId));

        // 2. Strict idempotency check for reference-based activities (e.g. LESSON_COMPLETED, QUIZ_PASSED)
        if (referenceId != null) {
            if (activityRepository.findByUserIdAndActivityTypeAndReferenceId(userId, activityType, referenceId).isPresent()) {
                log.debug("User {} already rewarded for activity {} with reference {}", userId, activityType, referenceId);
                return; // Already rewarded for this reference - idempotent no-op
            }
        }

        // 3. Log activity with database unique constraint safety net
        LearningActivity activity = LearningActivity.builder()
                .user(user)
                .activityType(activityType)
                .xpAmount(xpAmount)
                .referenceId(referenceId)
                .description(description)
                .build();

        try {
            activityRepository.saveAndFlush(activity);
        } catch (DataIntegrityViolationException ex) {
            // Safety net: in case concurrent insert bypassed application check
            if (referenceId != null) {
                log.warn("Concurrent duplicate reference reward caught by DB constraint for user {}, activity {}, ref {}",
                        userId, activityType, referenceId);
                return;
            }
            throw ex;
        }

        // 4. Update scores (ALL_TIME, WEEKLY, MONTHLY) using row locking / atomic pattern
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        String currentWeek = now.getYear() + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String currentMonth = now.getYear() + "-" + String.format("%02d", now.getMonthValue());

        UserScore allTimeScore = updateUserScore(user, "ALL_TIME", "ALL", xpAmount);
        updateUserScore(user, "WEEKLY", currentWeek, xpAmount);
        updateUserScore(user, "MONTHLY", currentMonth, xpAmount);

        // 5. Update User Streak (Preserve Duolingo-style daily streak behavior)
        if (user.getLastActivityDate() == null || user.getLastActivityDate().isBefore(now)) {
            if (user.getLastActivityDate() != null && user.getLastActivityDate().plusDays(1).equals(now)) {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
            } else if (user.getLastActivityDate() == null || user.getLastActivityDate().isBefore(now.minusDays(1))) {
                user.setCurrentStreak(1);
            }
            user.setLastActivityDate(now);
        }

        // 6. Update User Level (1 level per 1000 XP based on ALL_TIME score)
        if (allTimeScore != null && allTimeScore.getTotalXp() != null) {
            int newLevel = (allTimeScore.getTotalXp() / 1000) + 1;
            user.setLevel(newLevel);
        }

        userRepository.save(user);
    }

    private UserScore updateUserScore(User user, String periodType, String periodValue, Integer xp) {
        UserScore score = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValueForUpdate(user.getId(), periodType, periodValue)
                .orElseGet(() -> UserScore.builder()
                        .user(user)
                        .periodType(periodType)
                        .periodValue(periodValue)
                        .totalXp(0)
                        .build());

        score.setTotalXp(score.getTotalXp() + xp);
        return scoreRepository.saveAndFlush(score);
    }
}
