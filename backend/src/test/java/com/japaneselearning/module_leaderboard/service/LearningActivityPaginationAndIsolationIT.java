package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_leaderboard.dto.LearningActivityRes;
import com.japaneselearning.module_leaderboard.entity.LearningActivity;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.repository.LearningActivityRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LearningActivityPaginationAndIsolationIT {

    @Autowired
    private LearningActivityService activityService;

    @Autowired
    private LearningActivityRepository activityRepository;

    @Autowired
    private UserScoreRepository userScoreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    private User studentA;
    private User studentB;

    @BeforeEach
    void setUp() {
        studentA = userRepository.findByEmail("student_act_a@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_act_a@example.com")
                        .fullName("Student Act A")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .level(2)
                        .currentStreak(3)
                        .build()));

        studentB = userRepository.findByEmail("student_act_b@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_act_b@example.com")
                        .fullName("Student Act B")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .level(1)
                        .currentStreak(1)
                        .build()));
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

    private LearningActivity createActivity(User user, String type, int xp, Long refId, String desc, LocalDateTime createdAt) {
        LearningActivity activity = activityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType(type)
                .xpAmount(xp)
                .referenceId(refId)
                .description(desc)
                .build());

        if (createdAt != null) {
            jdbcTemplate.update("UPDATE learning_activities SET created_at = ? WHERE id = ?", createdAt, activity.getId());
            activity.setCreatedAt(createdAt);
        }
        return activity;
    }

    @Test
    @DisplayName("Pagination: Kiểm tra phân trang lịch sử hoạt động học tập nhiều trang với metadata chính xác")
    void testActivityPagination_MultiplePagesAndMetadata() {
        authenticateAs(studentA);

        LocalDateTime baseTime = LocalDateTime.of(2026, 11, 1, 9, 0, 0);
        for (int i = 1; i <= 5; i++) {
            createActivity(studentA, "LESSON_COMPLETED", 50 * i, (long) i, "Completed lesson " + i, baseTime.plusHours(i));
        }

        // Page 0, size 2
        PageResponse<LearningActivityRes> page0 = activityService.getMyActivities(PageRequest.of(0, 2));
        assertThat(page0.getCurrentPage()).isEqualTo(0);
        assertThat(page0.getPageSize()).isEqualTo(2);
        assertThat(page0.getTotalPages()).isEqualTo(3);
        assertThat(page0.getTotalElements()).isEqualTo(5);
        assertThat(page0.getData()).hasSize(2);
        assertThat(page0.getData().get(0).getXpAmount()).isEqualTo(250); // Activity 5 (latest created_at)
        assertThat(page0.getData().get(1).getXpAmount()).isEqualTo(200); // Activity 4

        // Page 1, size 2
        PageResponse<LearningActivityRes> page1 = activityService.getMyActivities(PageRequest.of(1, 2));
        assertThat(page1.getCurrentPage()).isEqualTo(1);
        assertThat(page1.getPageSize()).isEqualTo(2);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getData()).hasSize(2);
        assertThat(page1.getData().get(0).getXpAmount()).isEqualTo(150); // Activity 3
        assertThat(page1.getData().get(1).getXpAmount()).isEqualTo(100); // Activity 2

        // Page 2, size 2
        PageResponse<LearningActivityRes> page2 = activityService.getMyActivities(PageRequest.of(2, 2));
        assertThat(page2.getCurrentPage()).isEqualTo(2);
        assertThat(page2.getPageSize()).isEqualTo(2);
        assertThat(page2.getTotalPages()).isEqualTo(3);
        assertThat(page2.getTotalElements()).isEqualTo(5);
        assertThat(page2.getData()).hasSize(1);
        assertThat(page2.getData().get(0).getXpAmount()).isEqualTo(50); // Activity 1

        // Verify all activity IDs across pages are distinct
        Set<Long> collectedIds = new HashSet<>();
        page0.getData().forEach(a -> collectedIds.add(a.getId()));
        page1.getData().forEach(a -> collectedIds.add(a.getId()));
        page2.getData().forEach(a -> collectedIds.add(a.getId()));
        assertThat(collectedIds).hasSize(5);
    }

    @Test
    @DisplayName("Deterministic sorting: Tie-breaker id DESC khi các activity có cùng createdAt")
    void testDeterministicTieBreaker_WhenCreatedAtIsEqual() {
        authenticateAs(studentA);

        LocalDateTime sameTime = LocalDateTime.of(2026, 11, 10, 14, 0, 0);
        LearningActivity a1 = createActivity(studentA, "FLASHCARD_REVIEWED", 10, 101L, "Card 1", sameTime);
        LearningActivity a2 = createActivity(studentA, "FLASHCARD_REVIEWED", 20, 102L, "Card 2", sameTime);
        LearningActivity a3 = createActivity(studentA, "FLASHCARD_REVIEWED", 30, 103L, "Card 3", sameTime);

        // Sorting must deterministically break ties by id DESC
        PageResponse<LearningActivityRes> page0 = activityService.getMyActivities(PageRequest.of(0, 2));
        PageResponse<LearningActivityRes> page1 = activityService.getMyActivities(PageRequest.of(1, 2));

        assertThat(page0.getData()).hasSize(2);
        assertThat(page0.getData().get(0).getId()).isEqualTo(a3.getId());
        assertThat(page0.getData().get(1).getId()).isEqualTo(a2.getId());

        assertThat(page1.getData()).hasSize(1);
        assertThat(page1.getData().get(0).getId()).isEqualTo(a1.getId());
    }

    @Test
    @DisplayName("Cross-user isolation: Học viên chỉ thấy hoạt động của chính mình, tuyệt đối không lộ dữ liệu người khác")
    void testCrossUserIsolation_UserOnlySeesOwnActivities() {
        LocalDateTime now = LocalDateTime.now(clock);
        LearningActivity aA1 = createActivity(studentA, "QUIZ_PASSED", 100, 1L, "Quiz 1", now.minusHours(3));
        LearningActivity aA2 = createActivity(studentA, "QUIZ_PASSED", 200, 2L, "Quiz 2", now.minusHours(1));

        LearningActivity aB1 = createActivity(studentB, "QUIZ_PASSED", 80, 1L, "Quiz 1", now.minusHours(2));
        LearningActivity aB2 = createActivity(studentB, "QUIZ_PASSED", 120, 3L, "Quiz 3", now.minusMinutes(30));

        // 1. Authenticate as Student A
        authenticateAs(studentA);
        PageResponse<LearningActivityRes> pageA = activityService.getMyActivities(PageRequest.of(0, 10));

        assertThat(pageA.getTotalElements()).isEqualTo(2);
        List<Long> studentAIds = pageA.getData().stream().map(LearningActivityRes::getId).toList();
        assertThat(studentAIds).containsExactly(aA2.getId(), aA1.getId());
        assertThat(studentAIds).doesNotContain(aB1.getId(), aB2.getId());

        // 2. Authenticate as Student B
        authenticateAs(studentB);
        PageResponse<LearningActivityRes> pageB = activityService.getMyActivities(PageRequest.of(0, 10));

        assertThat(pageB.getTotalElements()).isEqualTo(2);
        List<Long> studentBIds = pageB.getData().stream().map(LearningActivityRes::getId).toList();
        assertThat(studentBIds).containsExactly(aB2.getId(), aB1.getId());
        assertThat(studentBIds).doesNotContain(aA1.getId(), aA2.getId());
    }

    @Test
    @DisplayName("Validation limits: Từ chối page âm, size <= 0, hoặc size > 100 với VALIDATION_ERROR")
    void testValidationLimits_ThrowsValidationError() {
        authenticateAs(studentA);

        Pageable negPage = mock(Pageable.class);
        when(negPage.getPageNumber()).thenReturn(-1);
        when(negPage.getPageSize()).thenReturn(10);
        assertThatThrownBy(() -> activityService.getMyActivities(negPage))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

        Pageable zeroSize = mock(Pageable.class);
        when(zeroSize.getPageNumber()).thenReturn(0);
        when(zeroSize.getPageSize()).thenReturn(0);
        assertThatThrownBy(() -> activityService.getMyActivities(zeroSize))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);

        Pageable overSize = mock(Pageable.class);
        when(overSize.getPageNumber()).thenReturn(0);
        when(overSize.getPageSize()).thenReturn(101);
        assertThatThrownBy(() -> activityService.getMyActivities(overSize))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("XP calculation independence: Tính tổng XP không phụ thuộc vào việc nạp toàn bộ lịch sử vào bộ nhớ")
    void testTotalXpCalculation_DoesNotLoadFullHistory() {
        // Setup UserScore with 1500 XP
        userScoreRepository.save(UserScore.builder()
                .user(studentA)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(1500)
                .build());

        // Also add some activities
        LocalDateTime now = LocalDateTime.now(clock);
        createActivity(studentA, "LESSON_COMPLETED", 50, 10L, "Lesson 10", now);
        createActivity(studentA, "QUIZ_PASSED", 100, 20L, "Quiz 20", now);

        // getTotalXpForUser uses UserScore directly (O(1)) without loading activities
        Long totalXp = activityService.getTotalXpForUser(studentA.getId());
        assertThat(totalXp).isEqualTo(1500L);

        // Test fallback on studentB who doesn't have UserScore record yet:
        createActivity(studentB, "LESSON_COMPLETED", 40, 30L, "Lesson 30", now);
        createActivity(studentB, "LESSON_COMPLETED", 60, 31L, "Lesson 31", now);

        // Uses SQL SUM() aggregation without loading list of entities into heap
        Long studentBXp = activityService.getTotalXpForUser(studentB.getId());
        assertThat(studentBXp).isEqualTo(100L);
    }

    @Test
    @DisplayName("Retention purge: Dọn dẹp hoạt động cũ hơn 180 ngày, bảo toàn 100% UserScore, Level và Streak")
    void testRetentionPurge_PreservesUserScoresAndLevels() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime oldDate = now.minusDays(200); // Beyond 180 days retention
        LocalDateTime recentDate = now.minusDays(30); // Within 180 days retention

        // Old activities
        LearningActivity oldAct1 = createActivity(studentA, "LESSON_COMPLETED", 50, 1L, "Old Lesson 1", oldDate);
        LearningActivity oldAct2 = createActivity(studentA, "QUIZ_PASSED", 100, 2L, "Old Quiz 2", oldDate);

        // Recent activities
        LearningActivity recentAct1 = createActivity(studentA, "LESSON_COMPLETED", 50, 3L, "Recent Lesson 3", recentDate);
        LearningActivity recentAct2 = createActivity(studentA, "DAILY_STREAK", 20, null, "Recent Streak", recentDate);

        // Persistent UserScore (10,000 XP) and Level 11
        userScoreRepository.save(UserScore.builder()
                .user(studentA)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(10000)
                .build());
        studentA.setLevel(11);
        studentA.setCurrentStreak(15);
        userRepository.save(studentA);

        // Execute retention purge with 180 days policy
        int deleted = activityService.purgeExpiredActivities(180);
        assertThat(deleted).isEqualTo(2);

        // Verify remaining activities
        authenticateAs(studentA);
        PageResponse<LearningActivityRes> page = activityService.getMyActivities(PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);
        List<Long> remainingIds = page.getData().stream().map(LearningActivityRes::getId).toList();
        assertThat(remainingIds).containsExactly(recentAct2.getId(), recentAct1.getId());
        assertThat(remainingIds).doesNotContain(oldAct1.getId(), oldAct2.getId());

        // CRITICAL INVARIANT: UserScore, Level, and Streak must remain 100% intact!
        UserScore scoreAfterPurge = userScoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(studentA.getId(), "ALL_TIME", "ALL").orElseThrow();
        assertThat(scoreAfterPurge.getTotalXp()).isEqualTo(10000);

        User userAfterPurge = userRepository.findById(studentA.getId()).orElseThrow();
        assertThat(userAfterPurge.getLevel()).isEqualTo(11);
        assertThat(userAfterPurge.getCurrentStreak()).isEqualTo(15);
    }
}
