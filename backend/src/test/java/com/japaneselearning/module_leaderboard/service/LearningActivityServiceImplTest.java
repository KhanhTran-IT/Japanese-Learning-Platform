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
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningActivityServiceImplTest {

    @Mock
    private LearningActivityRepository activityRepository;

    @Mock
    private UserScoreRepository scoreRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-11-20T10:00:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private LearningActivityServiceImpl service;

    private User student;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .email("student@test.com")
                .fullName("Test Student")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuth() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("student@test.com", "password"));
        SecurityContextHolder.setContext(context);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
    }

    @Test
    @DisplayName("getMyActivities: Returns paginated activities with deterministic sorting")
    void getMyActivities_Success() {
        mockAuth();

        LocalDateTime now = LocalDateTime.now(clock);
        LearningActivity a1 = LearningActivity.builder()
                .id(101L).user(student).activityType("LESSON_COMPLETED").xpAmount(50)
                .referenceId(1L).description("Lesson 1").createdAt(now).build();
        LearningActivity a2 = LearningActivity.builder()
                .id(100L).user(student).activityType("QUIZ_PASSED").xpAmount(100)
                .referenceId(2L).description("Quiz 2").createdAt(now.minusHours(1)).build();

        Page<LearningActivity> page = new PageImpl<>(List.of(a1, a2), PageRequest.of(0, 10), 2);
        when(activityRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        PageResponse<LearningActivityRes> res = service.getMyActivities(PageRequest.of(0, 10));

        assertThat(res.getCurrentPage()).isEqualTo(0);
        assertThat(res.getPageSize()).isEqualTo(10);
        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getData()).hasSize(2);
        assertThat(res.getData().get(0).getId()).isEqualTo(101L);
        assertThat(res.getData().get(0).getXpAmount()).isEqualTo(50);
        assertThat(res.getData().get(1).getId()).isEqualTo(100L);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(activityRepository).findByUserId(eq(1L), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        assertThat(captured.getSort().getOrderFor("id").isDescending()).isTrue();
    }

    @Test
    @DisplayName("getMyActivities: Rejects page < 0 with VALIDATION_ERROR")
    void getMyActivities_NegativePage_ThrowsValidation() {
        mockAuth();
        Pageable mockPageable = mock(Pageable.class);
        when(mockPageable.getPageNumber()).thenReturn(-1);
        when(mockPageable.getPageSize()).thenReturn(10);

        assertThatThrownBy(() -> service.getMyActivities(mockPageable))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("getMyActivities: Rejects size <= 0 with VALIDATION_ERROR")
    void getMyActivities_ZeroSize_ThrowsValidation() {
        mockAuth();
        Pageable mockPageable = mock(Pageable.class);
        when(mockPageable.getPageNumber()).thenReturn(0);
        when(mockPageable.getPageSize()).thenReturn(0);

        assertThatThrownBy(() -> service.getMyActivities(mockPageable))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("getMyActivities: Rejects size > 100 with VALIDATION_ERROR")
    void getMyActivities_OversizedSize_ThrowsValidation() {
        mockAuth();
        Pageable mockPageable = mock(Pageable.class);
        when(mockPageable.getPageNumber()).thenReturn(0);
        when(mockPageable.getPageSize()).thenReturn(101);

        assertThatThrownBy(() -> service.getMyActivities(mockPageable))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("getTotalXpForUser: Reads from UserScore ALL_TIME directly without querying activities")
    void getTotalXpForUser_UserScorePresent() {
        UserScore score = UserScore.builder().user(student).periodType("ALL_TIME").periodValue("ALL").totalXp(3500).build();
        when(scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(1L, "ALL_TIME", "ALL")).thenReturn(Optional.of(score));

        Long totalXp = service.getTotalXpForUser(1L);
        assertThat(totalXp).isEqualTo(3500L);
        verifyNoInteractions(activityRepository);
    }

    @Test
    @DisplayName("getTotalXpForUser: Falls back to SQL SUM aggregate query when UserScore is not present")
    void getTotalXpForUser_FallbackToSqlSum() {
        when(scoreRepository.findByUserIdAndPeriodTypeAndPeriodValue(1L, "ALL_TIME", "ALL")).thenReturn(Optional.empty());
        when(activityRepository.sumXpByUserId(1L)).thenReturn(450L);

        Long totalXp = service.getTotalXpForUser(1L);
        assertThat(totalXp).isEqualTo(450L);
        verify(activityRepository).sumXpByUserId(1L);
    }

    @Test
    @DisplayName("getTotalXpForUser: Null userId safely returns 0")
    void getTotalXpForUser_NullUser() {
        assertThat(service.getTotalXpForUser(null)).isEqualTo(0L);
    }

    @Test
    @DisplayName("purgeExpiredActivities: Rejects retentionDays < 30 with VALIDATION_ERROR")
    void purgeExpiredActivities_TooSmallRetention_Throws() {
        assertThatThrownBy(() -> service.purgeExpiredActivities(29))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    @DisplayName("purgeExpiredActivities: Calculates cutoff date and purges old activities")
    void purgeExpiredActivities_Success() {
        when(activityRepository.deleteByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(15);
        when(activityRepository.countByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(15L);

        int deleted = service.purgeExpiredActivities(180);

        assertThat(deleted).isEqualTo(15);
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(activityRepository).deleteByCreatedAtBefore(cutoffCaptor.capture());
        LocalDateTime capturedCutoff = cutoffCaptor.getValue();

        LocalDateTime expectedCutoff = LocalDateTime.now(clock).minusDays(180);
        assertThat(capturedCutoff).isEqualTo(expectedCutoff);
    }
}
