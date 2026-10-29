package com.japaneselearning.module_quiz.service;

import com.japaneselearning.common.observability.ObservabilityMetrics;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_leaderboard.service.ScoringService;
import com.japaneselearning.module_quiz.dto.QuizDiscoveryRes;
import com.japaneselearning.module_quiz.dto.QuizAttemptSummaryRes;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.entity.QuizAttempt;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.AnswerRepository;
import com.japaneselearning.module_quiz.repository.QuestionRepository;
import com.japaneselearning.module_quiz.repository.QuizAttemptAnswerRepository;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
import com.japaneselearning.module_quiz.repository.QuizRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizPerformanceRegressionTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuizAttemptRepository attemptRepository;

    @Mock
    private QuizAttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScoringService scoringService;

    @Mock
    private ObservabilityMetrics observabilityMetrics;

    @InjectMocks
    private QuizLearningServiceImpl quizLearningService;

    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .email("student@test.com")
                .fullName("Test Student")
                .build();

        course = Course.builder()
                .id(10L)
                .title("N5 Japanese")
                .status(CourseStatus.PUBLISHED)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("student@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getMyAttempts should load attempts and map quiz info eagerly without individual quiz lookups")
    void getMyAttempts_shouldMapAttemptsEagerly() {
        Quiz quiz = Quiz.builder()
                .id(100L)
                .title("Kanji Quiz 1")
                .passingScore(BigDecimal.valueOf(80.0))
                .build();

        QuizAttempt attempt = QuizAttempt.builder()
                .id(501L)
                .user(student)
                .quiz(quiz)
                .score(BigDecimal.valueOf(90.0))
                .totalQuestions(10)
                .correctCount(9)
                .passed(true)
                .status(QuizAttemptStatus.SUBMITTED)
                .startedAt(LocalDateTime.now().minusHours(1))
                .submittedAt(LocalDateTime.now())
                .build();

        when(attemptRepository.findByUserIdOrderByStartedAtDesc(student.getId()))
                .thenReturn(List.of(attempt));

        List<QuizAttemptSummaryRes> result = quizLearningService.getMyAttempts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuizTitle()).isEqualTo("Kanji Quiz 1");
        assertThat(result.get(0).getPassingScore()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
        assertThat(result.get(0).getPassed()).isTrue();

        verify(attemptRepository, times(1)).findByUserIdOrderByStartedAtDesc(student.getId());
        // Verify no individual findById calls for quizzes
        verifyNoInteractions(quizRepository);
    }

    @Test
    @DisplayName("getLessonQuizzes should use batch queries instead of N+1 per-quiz queries")
    void getLessonQuizzes_shouldUseBatchQueries_avoidingNPlusOne() {
        Long lessonId = 20L;

        Quiz quiz1 = Quiz.builder()
                .id(101L)
                .course(course)
                .title("Quiz 1")
                .status(QuizStatus.PUBLISHED)
                .maxAttempts(3)
                .passingScore(BigDecimal.valueOf(70.0))
                .build();

        Quiz quiz2 = Quiz.builder()
                .id(102L)
                .course(course)
                .title("Quiz 2")
                .status(QuizStatus.PUBLISHED)
                .maxAttempts(2)
                .passingScore(BigDecimal.valueOf(80.0))
                .build();

        when(quizRepository.findByLessonIdAndStatus(lessonId, QuizStatus.PUBLISHED))
                .thenReturn(List.of(quiz1, quiz2));

        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(eq(student.getId()), eq(course.getId()), any()))
                .thenReturn(true);

        List<Long> expectedQuizIds = List.of(101L, 102L);

        // Batch question counts
        List<Object[]> questionCounts = List.of(
                new Object[]{101L, 5L},
                new Object[]{102L, 10L}
        );
        when(questionRepository.countQuestionsByQuizIds(expectedQuizIds))
                .thenReturn(questionCounts);

        // Batch attempt counts
        List<Object[]> attemptCounts = List.of(
                new Object[]{101L, 1L},
                new Object[]{102L, 2L}
        );
        when(attemptRepository.countAttemptsByUserIdAndQuizIds(student.getId(), expectedQuizIds))
                .thenReturn(attemptCounts);

        // Batch latest attempts
        QuizAttempt latestQ1 = QuizAttempt.builder()
                .id(601L)
                .quiz(quiz1)
                .user(student)
                .status(QuizAttemptStatus.SUBMITTED)
                .score(BigDecimal.valueOf(85.0))
                .passed(true)
                .startedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(attemptRepository.findByUserIdAndQuizIdInOrderByStartedAtDesc(student.getId(), expectedQuizIds))
                .thenReturn(List.of(latestQ1));

        List<QuizDiscoveryRes> result = quizLearningService.getLessonQuizzes(lessonId);

        assertThat(result).hasSize(2);

        // Quiz 1 assertions
        QuizDiscoveryRes res1 = result.stream().filter(r -> r.getId().equals(101L)).findFirst().orElseThrow();
        assertThat(res1.getQuestionCount()).isEqualTo(5);
        assertThat(res1.getRemainingAttempts()).isEqualTo(2); // 3 max - 1 used
        assertThat(res1.getLatestAttemptId()).isEqualTo(601L);
        assertThat(res1.getLatestScore()).isEqualByComparingTo(BigDecimal.valueOf(85.0));

        // Quiz 2 assertions
        QuizDiscoveryRes res2 = result.stream().filter(r -> r.getId().equals(102L)).findFirst().orElseThrow();
        assertThat(res2.getQuestionCount()).isEqualTo(10);
        assertThat(res2.getRemainingAttempts()).isEqualTo(0); // 2 max - 2 used
        assertThat(res2.getLatestAttemptId()).isNull();

        // Critical regression check: batch methods called exactly ONCE each, NOT in a loop
        verify(questionRepository, times(1)).countQuestionsByQuizIds(expectedQuizIds);
        verify(attemptRepository, times(1)).countAttemptsByUserIdAndQuizIds(student.getId(), expectedQuizIds);
        verify(attemptRepository, times(1)).findByUserIdAndQuizIdInOrderByStartedAtDesc(student.getId(), expectedQuizIds);

        // Verify per-quiz non-batch methods were NEVER invoked
        verify(questionRepository, never()).countByQuizId(any());
        verify(attemptRepository, never()).countByUserIdAndQuizId(any(), any());
        verify(attemptRepository, never()).findFirstByUserIdAndQuizIdOrderByStartedAtDesc(any(), any());
    }

    @Test
    @DisplayName("getLessonQuizzes should return empty when no published quizzes exist")
    void getLessonQuizzes_shouldReturnEmptyWhenNoQuizzes() {
        Long lessonId = 30L;
        when(quizRepository.findByLessonIdAndStatus(lessonId, QuizStatus.PUBLISHED))
                .thenReturn(List.of());

        List<QuizDiscoveryRes> result = quizLearningService.getLessonQuizzes(lessonId);

        assertThat(result).isEmpty();
        verifyNoInteractions(questionRepository);
        verifyNoInteractions(attemptRepository);
    }
}
