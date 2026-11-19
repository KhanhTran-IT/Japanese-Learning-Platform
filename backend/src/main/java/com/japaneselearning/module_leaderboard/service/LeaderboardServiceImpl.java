package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_leaderboard.dto.LeaderboardRes;
import com.japaneselearning.module_leaderboard.dto.LeaderboardUserRes;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.repository.UserScoreRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final UserScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public LeaderboardServiceImpl(UserScoreRepository scoreRepository, UserRepository userRepository) {
        this(scoreRepository, userRepository, Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public LeaderboardServiceImpl(UserScoreRepository scoreRepository, UserRepository userRepository, Clock clock) {
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
    // Personalized response containing user-specific data (isMe, currentUserRanking)
    // must NOT be cached under a generic public key to avoid cross-user cache pollution.
    public LeaderboardRes getLeaderboard(String timeframe, int page, int size) {
        if (page < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be less than one");
        }
        if (size > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Page size must not be greater than 100");
        }

        User currentUser = getCurrentUser();
        
        String periodType = (timeframe == null || timeframe.isBlank()) ? "ALL_TIME" : timeframe.trim().toUpperCase();
        if (!"WEEKLY".equals(periodType) && !"MONTHLY".equals(periodType) && !"ALL_TIME".equals(periodType)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid timeframe: " + timeframe + ". Expected WEEKLY, MONTHLY, or ALL_TIME");
        }
        String periodValue = getPeriodValue(periodType);

        Page<UserScore> scorePage = scoreRepository.findTopByPeriod(periodType, periodValue, PageRequest.of(page, size));
        
        List<LeaderboardUserRes> rankings = scorePage.getContent().stream()
                .map(us -> mapToUserRes(us, currentUser.getId(), calculateRank(page, size, scorePage.getContent().indexOf(us))))
                .collect(Collectors.toList());

        // Get current user's rank
        LeaderboardUserRes myRanking = null;
        if (!currentUser.getIsPrivateLeaderboard()) {
            Long myRank = scoreRepository.getRankByUserIdAndPeriod(currentUser.getId(), periodType, periodValue);
            if (myRank != null) {
                UserScore myScore = scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(currentUser.getId(), periodType, periodValue).orElse(null);
                if (myScore != null) {
                    myRanking = mapToUserRes(myScore, currentUser.getId(), myRank);
                }
            }
        }

        return LeaderboardRes.builder()
                .rankings(rankings)
                .currentUserRanking(myRanking)
                .build();
    }

    private String getPeriodValue(String periodType) {
        LocalDate now = clock.instant().atZone(ZoneOffset.UTC).toLocalDate();
        if ("WEEKLY".equals(periodType)) {
            return now.getYear() + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        } else if ("MONTHLY".equals(periodType)) {
            return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
        }
        return "ALL";
    }

    private Long calculateRank(int page, int size, int index) {
        return (long) page * size + index + 1;
    }

    private LeaderboardUserRes mapToUserRes(UserScore us, Long currentUserId, Long rank) {
        User u = us.getUser();
        String badge = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "🏅";
        
        String initial = u.getFullName() != null && !u.getFullName().isEmpty() 
            ? String.valueOf(u.getFullName().charAt(0)).toUpperCase() 
            : "U";
            
        return LeaderboardUserRes.builder()
                .rank(rank)
                .name(u.getFullName())
                .avatar(u.getAvatarUrl() != null ? u.getAvatarUrl() : initial)
                .xp(us.getTotalXp())
                .level(u.getLevel())
                .streak(u.getCurrentStreak())
                .badge(badge)
                .country(u.getCountry())
                .isMe(u.getId().equals(currentUserId))
                .build();
    }
}
