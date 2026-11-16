package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_leaderboard.dto.LeaderboardRes;
import com.japaneselearning.module_leaderboard.dto.LeaderboardUserRes;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.repository.UserScoreRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LeaderboardPaginationAndDeterministicIT {

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private UserScoreRepository userScoreRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.builder()
                .email("student_lb_a@example.com")
                .fullName("Student A")
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .isPrivateLeaderboard(false)
                .level(3)
                .currentStreak(2)
                .build());

        userB = userRepository.save(User.builder()
                .email("student_lb_b@example.com")
                .fullName("Student B")
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .isPrivateLeaderboard(false)
                .level(4)
                .currentStreak(5)
                .build());

        userC = userRepository.save(User.builder()
                .email("student_lb_c@example.com")
                .fullName("Student C")
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .isPrivateLeaderboard(false)
                .level(5)
                .currentStreak(7)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), "password"));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Deterministic ordering: Tie-breaker khi các học viên có cùng totalXp")
    void testDeterministicTieBreaker_WhenXpIsIdentical() {
        authenticateAs(userA);

        // Given: All 3 users have identical totalXp = 500
        UserScore scoreA = userScoreRepository.save(UserScore.builder()
                .user(userA)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(500)
                .build());

        UserScore scoreB = userScoreRepository.save(UserScore.builder()
                .user(userB)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(500)
                .build());

        UserScore scoreC = userScoreRepository.save(UserScore.builder()
                .user(userC)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(500)
                .build());

        // When fetching leaderboard page 0 size 2, and page 1 size 2
        LeaderboardRes page0 = leaderboardService.getLeaderboard("ALL_TIME", 0, 2);
        LeaderboardRes page1 = leaderboardService.getLeaderboard("ALL_TIME", 1, 2);

        // Ordering must be deterministic: ordered by totalXp DESC, us.id ASC
        assertThat(page0.getRankings()).hasSize(2);
        assertThat(page1.getRankings()).hasSize(1);

        // The tie-breaker us.id ASC ensures: scoreA, then scoreB on page 0, and scoreC on page 1
        assertThat(page0.getRankings().get(0).getName()).isEqualTo("Student A");
        assertThat(page0.getRankings().get(0).getRank()).isEqualTo(1L);

        assertThat(page0.getRankings().get(1).getName()).isEqualTo("Student B");
        assertThat(page0.getRankings().get(1).getRank()).isEqualTo(2L);

        assertThat(page1.getRankings().get(0).getName()).isEqualTo("Student C");
        assertThat(page1.getRankings().get(0).getRank()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Safe limits: Validates parameters with safe limits and defaults")
    void testLeaderboardSafeLimitsAndValidation() {
        authenticateAs(userA);

        assertThatThrownBy(() -> leaderboardService.getLeaderboard("ALL_TIME", -1, 50))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

        assertThatThrownBy(() -> leaderboardService.getLeaderboard("ALL_TIME", 0, 0))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

        assertThatThrownBy(() -> leaderboardService.getLeaderboard("ALL_TIME", 0, 101))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

        assertThatThrownBy(() -> leaderboardService.getLeaderboard("INVALID_TIMEFRAME", 0, 50))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("Privacy: Người dùng ẩn bảng xếp hạng không xuất hiện trong danh sách")
    void testPrivateLeaderboard_UserHiddenFromRankings() {
        userB.setIsPrivateLeaderboard(true);
        userRepository.save(userB);

        userScoreRepository.save(UserScore.builder()
                .user(userA)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(300)
                .build());

        userScoreRepository.save(UserScore.builder()
                .user(userB)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(500)
                .build());

        authenticateAs(userA);
        LeaderboardRes res = leaderboardService.getLeaderboard("ALL_TIME", 0, 50);

        List<String> names = res.getRankings().stream().map(LeaderboardUserRes::getName).toList();
        assertThat(names).contains("Student A");
        assertThat(names).doesNotContain("Student B");
    }
}
