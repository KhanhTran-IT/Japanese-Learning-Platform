package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_leaderboard.dto.LearningActivityRes;
import com.japaneselearning.module_leaderboard.entity.LearningActivity;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.policy.LearningActivityRetentionPolicy;
import com.japaneselearning.module_leaderboard.repository.LearningActivityRepository;
import com.japaneselearning.module_leaderboard.repository.UserScoreRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class LearningActivityServiceImpl implements LearningActivityService {

    private final LearningActivityRepository activityRepository;
    private final UserScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public LearningActivityServiceImpl(
            LearningActivityRepository activityRepository,
            UserScoreRepository scoreRepository,
            UserRepository userRepository
    ) {
        this(activityRepository, scoreRepository, userRepository, Clock.systemUTC());
    }

    @Autowired
    public LearningActivityServiceImpl(
            LearningActivityRepository activityRepository,
            UserScoreRepository scoreRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.activityRepository = activityRepository;
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LearningActivityRes> getMyActivities(Pageable pageable) {
        User currentUser = getCurrentUser();

        int page = pageable != null ? pageable.getPageNumber() : 0;
        int size = pageable != null ? pageable.getPageSize() : 10;

        if (page < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be less than one");
        }
        if (size > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be greater than 100");
        }

        // Enforce deterministic sorting: createdAt DESC, id DESC
        Pageable deterministicPageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        Page<LearningActivity> activityPage = activityRepository.findByUserId(currentUser.getId(), deterministicPageable);

        return PageResponse.<LearningActivityRes>builder()
                .currentPage(activityPage.getNumber())
                .pageSize(activityPage.getSize())
                .totalPages(activityPage.getTotalPages())
                .totalElements(activityPage.getTotalElements())
                .data(activityPage.getContent().stream()
                        .map(this::mapToRes)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalXpForUser(Long userId) {
        if (userId == null) {
            return 0L;
        }

        // 1. Try reading from UserScore (ALL_TIME) single-row lookup first (fastest O(1))
        Optional<UserScore> allTimeScore = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(userId, "ALL_TIME", "ALL");
        if (allTimeScore.isPresent() && allTimeScore.get().getTotalXp() != null) {
            return allTimeScore.get().getTotalXp().longValue();
        }

        // 2. Fallback to SQL SUM() aggregate query - executes in DB engine without loading entities into heap
        return activityRepository.sumXpByUserId(userId);
    }

    @Override
    @Transactional
    public int purgeExpiredActivities(int retentionDays) {
        if (retentionDays < LearningActivityRetentionPolicy.MIN_RETENTION_DAYS) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Retention days must be at least " + LearningActivityRetentionPolicy.MIN_RETENTION_DAYS + " days");
        }

        LocalDateTime cutoff = LocalDateTime.now(clock).minusDays(retentionDays);
        long eligibleCount = activityRepository.countByCreatedAtBefore(cutoff);
        log.info("Starting retention purge for learning activities older than {} (retentionDays={}). Eligible rows: {}",
                cutoff, retentionDays, eligibleCount);

        int deletedCount = activityRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Completed retention purge: successfully deleted {} historical learning activity records", deletedCount);

        return deletedCount;
    }

    private LearningActivityRes mapToRes(LearningActivity activity) {
        return LearningActivityRes.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .xpAmount(activity.getXpAmount())
                .referenceId(activity.getReferenceId())
                .description(activity.getDescription())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
