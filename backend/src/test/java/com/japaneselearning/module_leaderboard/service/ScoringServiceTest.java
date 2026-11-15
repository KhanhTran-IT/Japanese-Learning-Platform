package com.japaneselearning.module_leaderboard.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_leaderboard.entity.LearningActivity;
import com.japaneselearning.module_leaderboard.entity.UserScore;
import com.japaneselearning.module_leaderboard.repository.LearningActivityRepository;
import com.japaneselearning.module_leaderboard.repository.UserScoreRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock
    private LearningActivityRepository activityRepository;

    @Mock
    private UserScoreRepository scoreRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScoringService scoringService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("student@example.com")
                .fullName("Test Student")
                .level(1)
                .currentStreak(0)
                .lastActivityDate(null)
                .build();
    }

    @Test
    @DisplayName("addXp: Null or invalid input should be ignored safely")
    void addXp_NullOrInvalidInput_Ignored() {
        scoringService.addXp(null, "LESSON_COMPLETED", 100, 1L, "Desc");
        scoringService.addXp(1L, null, 100, 1L, "Desc");
        scoringService.addXp(1L, "LESSON_COMPLETED", null, 1L, "Desc");
        scoringService.addXp(1L, "LESSON_COMPLETED", 0, 1L, "Desc");
        scoringService.addXp(1L, "LESSON_COMPLETED", -50, 1L, "Desc");

        verifyNoInteractions(userRepository, activityRepository, scoreRepository);
    }

    @Test
    @DisplayName("addXp: User not found throws AppException")
    void addXp_UserNotFound_ThrowsException() {
        when(userRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scoringService.addXp(99L, "LESSON_COMPLETED", 100, 1L, "Desc"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(activityRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("addXp: Reference-based activity already exists -> idempotent no-op")
    void addXp_ReferenceBasedActivity_AlreadyExists_IdempotentNoOp() {
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.findByUserIdAndActivityTypeAndReferenceId(1L, "LESSON_COMPLETED", 100L))
                .thenReturn(Optional.of(new LearningActivity()));

        scoringService.addXp(1L, "LESSON_COMPLETED", 100, 100L, "Hoàn thành bài 100");

        verify(activityRepository, never()).saveAndFlush(any());
        verify(scoreRepository, never()).saveAndFlush(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("addXp: Reference-based activity new -> successfully logs activity and updates scores, streak, and level")
    void addXp_ReferenceBasedActivity_New_Success() {
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.findByUserIdAndActivityTypeAndReferenceId(1L, "LESSON_COMPLETED", 100L))
                .thenReturn(Optional.empty());

        when(scoreRepository.findByUserIdAndPeriodTypeAndPeriodValueForUpdate(eq(1L), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(scoreRepository.saveAndFlush(any(UserScore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scoringService.addXp(1L, "LESSON_COMPLETED", 100, 100L, "Hoàn thành bài 100");

        // Verify activity saved
        ArgumentCaptor<LearningActivity> activityCaptor = ArgumentCaptor.forClass(LearningActivity.class);
        verify(activityRepository).saveAndFlush(activityCaptor.capture());
        assertThat(activityCaptor.getValue().getXpAmount()).isEqualTo(100);
        assertThat(activityCaptor.getValue().getReferenceId()).isEqualTo(100L);

        // Verify 3 scores updated (ALL_TIME, WEEKLY, MONTHLY)
        verify(scoreRepository, times(3)).saveAndFlush(any(UserScore.class));

        // Verify user streak and activity date updated
        assertThat(testUser.getCurrentStreak()).isEqualTo(1);
        assertThat(testUser.getLastActivityDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("addXp: Reference-based activity caught by DB unique constraint -> safely ignored")
    void addXp_ReferenceBasedActivity_CaughtByDbConstraint_IgnoredSafely() {
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.findByUserIdAndActivityTypeAndReferenceId(1L, "LESSON_COMPLETED", 100L))
                .thenReturn(Optional.empty());

        doThrow(new DataIntegrityViolationException("Duplicate key"))
                .when(activityRepository).saveAndFlush(any(LearningActivity.class));

        scoringService.addXp(1L, "LESSON_COMPLETED", 100, 100L, "Race condition duplicate");

        // Did not update scores due to duplicate
        verify(scoreRepository, never()).saveAndFlush(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("addXp: Nullable reference activity -> proceeds without checking reference idempotency")
    void addXp_NullableReference_Success() {
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
        when(scoreRepository.findByUserIdAndPeriodTypeAndPeriodValueForUpdate(eq(1L), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(scoreRepository.saveAndFlush(any(UserScore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scoringService.addXp(1L, "FLASHCARD_REVIEWED", 10, null, "Ôn tập thẻ");

        verify(activityRepository, never()).findByUserIdAndActivityTypeAndReferenceId(any(), any(), any());

        ArgumentCaptor<LearningActivity> activityCaptor = ArgumentCaptor.forClass(LearningActivity.class);
        verify(activityRepository).saveAndFlush(activityCaptor.capture());
        assertThat(activityCaptor.getValue().getReferenceId()).isNull();
        assertThat(activityCaptor.getValue().getXpAmount()).isEqualTo(10);
    }

    @Test
    @DisplayName("addXp: Streak consecutive days increments streak by 1")
    void addXp_StreakConsecutiveDays_IncrementsStreak() {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        testUser.setCurrentStreak(5);
        testUser.setLastActivityDate(yesterday);

        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
        when(scoreRepository.saveAndFlush(any(UserScore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scoringService.addXp(1L, "FLASHCARD_REVIEWED", 10, null, "Ôn tập thẻ");

        assertThat(testUser.getCurrentStreak()).isEqualTo(6);
        assertThat(testUser.getLastActivityDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
    }

    @Test
    @DisplayName("addXp: Streak same day activity preserves existing streak")
    void addXp_StreakSameDay_PreservesStreak() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        testUser.setCurrentStreak(5);
        testUser.setLastActivityDate(today);

        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
        when(scoreRepository.saveAndFlush(any(UserScore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scoringService.addXp(1L, "FLASHCARD_REVIEWED", 10, null, "Ôn tập thẻ lần 2");

        assertThat(testUser.getCurrentStreak()).isEqualTo(5);
        assertThat(testUser.getLastActivityDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("addXp: Streak broken (> 1 day missed) resets streak to 1")
    void addXp_StreakBroken_ResetsToOne() {
        LocalDate threeDaysAgo = LocalDate.now(ZoneOffset.UTC).minusDays(3);
        testUser.setCurrentStreak(10);
        testUser.setLastActivityDate(threeDaysAgo);

        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));
        when(scoreRepository.saveAndFlush(any(UserScore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scoringService.addXp(1L, "FLASHCARD_REVIEWED", 10, null, "Quay lại học");

        assertThat(testUser.getCurrentStreak()).isEqualTo(1);
        assertThat(testUser.getLastActivityDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
    }

    @Test
    @DisplayName("addXp: Level promotion triggers when total XP crosses 1000 boundary")
    void addXp_LevelPromotion_CalculatedFromAllTimeXp() {
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(testUser));

        UserScore existingAllTime = UserScore.builder()
                .user(testUser)
                .periodType("ALL_TIME")
                .periodValue("ALL")
                .totalXp(950)
                .build();

        when(scoreRepository.findByUserIdAndPeriodTypeAndPeriodValueForUpdate(1L, "ALL_TIME", "ALL"))
                .thenReturn(Optional.of(existingAllTime));
        when(scoreRepository.saveAndFlush(any(UserScore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Add 100 XP -> total 1050 XP -> Level 2
        scoringService.addXp(1L, "LESSON_COMPLETED", 100, 999L, "Bài học lên level");

        assertThat(existingAllTime.getTotalXp()).isEqualTo(1050);
        assertThat(testUser.getLevel()).isEqualTo(2); // (1050 / 1000) + 1 = 2
    }
}
