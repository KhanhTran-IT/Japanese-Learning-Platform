package com.japaneselearning.module_quiz.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_quiz.dto.QuizAttemptSummaryRes;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.entity.QuizAttempt;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
import com.japaneselearning.module_quiz.repository.QuizRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class QuizAttemptPaginationAndIsolationIT {

    @Autowired
    private QuizLearningService quizLearningService;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    private User studentA;
    private User studentB;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        studentA = userRepository.findByEmail("student_quiz_a@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_quiz_a@example.com")
                        .fullName("Student Quiz A")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        studentB = userRepository.findByEmail("student_quiz_b@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_quiz_b@example.com")
                        .fullName("Student Quiz B")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        User teacher = userRepository.findByEmail("teacher_quiz_it@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("teacher_quiz_it@example.com")
                        .fullName("Teacher Quiz IT")
                        .passwordHash("hashed")
                        .status(UserStatus.ACTIVE)
                        .build()));

        Course course = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Course for Quiz Pagination")
                .slug("course-quiz-pagination-it")
                .level(CourseLevel.N3)
                .courseType(CourseType.FREE)
                .status(CourseStatus.PUBLISHED)
                .totalStudents(0)
                .build());

        quiz = quizRepository.save(Quiz.builder()
                .course(course)
                .title("Quiz JLPT N3 Grammar")
                .description("Test quiz for pagination")
                .passingScore(BigDecimal.valueOf(80.0))
                .maxAttempts(10)
                .status(QuizStatus.PUBLISHED)
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

    private QuizAttempt createAttempt(User user, int attemptNumber, double score, LocalDateTime startedAt) {
        return attemptRepository.save(QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .attemptNumber(attemptNumber)
                .score(BigDecimal.valueOf(score))
                .totalQuestions(10)
                .correctCount((int) (score / 10))
                .passed(score >= 80.0)
                .status(QuizAttemptStatus.SUBMITTED)
                .startedAt(startedAt)
                .submittedAt(startedAt.plusMinutes(15))
                .build());
    }

    @Test
    @DisplayName("Pagination: Kiểm tra phân trang lịch sử làm bài nhiều trang với metadata chính xác")
    void testQuizAttemptPagination_MultiplePagesAndMetadata() {
        authenticateAs(studentA);

        LocalDateTime baseTime = LocalDateTime.of(2026, 11, 1, 9, 0, 0);
        for (int i = 1; i <= 5; i++) {
            createAttempt(studentA, i, 60.0 + i * 5, baseTime.plusHours(i));
        }

        // Page 0, size 2
        PageResponse<QuizAttemptSummaryRes> page0 = quizLearningService.getMyAttempts(PageRequest.of(0, 2));
        assertThat(page0.getCurrentPage()).isEqualTo(0);
        assertThat(page0.getPageSize()).isEqualTo(2);
        assertThat(page0.getTotalPages()).isEqualTo(3);
        assertThat(page0.getTotalElements()).isEqualTo(5);
        assertThat(page0.getData()).hasSize(2);
        assertThat(page0.getData().get(0).getScore()).isEqualByComparingTo(BigDecimal.valueOf(85.0)); // attempt 5 (latest startedAt)
        assertThat(page0.getData().get(1).getScore()).isEqualByComparingTo(BigDecimal.valueOf(80.0)); // attempt 4

        // Page 1, size 2
        PageResponse<QuizAttemptSummaryRes> page1 = quizLearningService.getMyAttempts(PageRequest.of(1, 2));
        assertThat(page1.getCurrentPage()).isEqualTo(1);
        assertThat(page1.getPageSize()).isEqualTo(2);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getData()).hasSize(2);
        assertThat(page1.getData().get(0).getScore()).isEqualByComparingTo(BigDecimal.valueOf(75.0)); // attempt 3
        assertThat(page1.getData().get(1).getScore()).isEqualByComparingTo(BigDecimal.valueOf(70.0)); // attempt 2

        // Page 2, size 2
        PageResponse<QuizAttemptSummaryRes> page2 = quizLearningService.getMyAttempts(PageRequest.of(2, 2));
        assertThat(page2.getCurrentPage()).isEqualTo(2);
        assertThat(page2.getPageSize()).isEqualTo(2);
        assertThat(page2.getTotalPages()).isEqualTo(3);
        assertThat(page2.getTotalElements()).isEqualTo(5);
        assertThat(page2.getData()).hasSize(1);
        assertThat(page2.getData().get(0).getScore()).isEqualByComparingTo(BigDecimal.valueOf(65.0)); // attempt 1

        // Verify all attempt IDs collected are unique across pages
        Set<Long> collectedIds = new HashSet<>();
        page0.getData().forEach(a -> collectedIds.add(a.getAttemptId()));
        page1.getData().forEach(a -> collectedIds.add(a.getAttemptId()));
        page2.getData().forEach(a -> collectedIds.add(a.getAttemptId()));
        assertThat(collectedIds).hasSize(5);
    }

    @Test
    @DisplayName("Deterministic sorting: Tie-breaker id DESC khi các attempt có cùng startedAt")
    void testDeterministicTieBreaker_WhenStartedAtIsEqual() {
        authenticateAs(studentA);

        LocalDateTime sameTime = LocalDateTime.of(2026, 11, 10, 14, 0, 0);
        QuizAttempt a1 = createAttempt(studentA, 1, 70.0, sameTime);
        QuizAttempt a2 = createAttempt(studentA, 2, 80.0, sameTime);
        QuizAttempt a3 = createAttempt(studentA, 3, 90.0, sameTime);

        // Sorting must deterministically break ties by id DESC
        PageResponse<QuizAttemptSummaryRes> page0 = quizLearningService.getMyAttempts(PageRequest.of(0, 2));
        PageResponse<QuizAttemptSummaryRes> page1 = quizLearningService.getMyAttempts(PageRequest.of(1, 2));

        assertThat(page0.getData()).hasSize(2);
        assertThat(page0.getData().get(0).getAttemptId()).isEqualTo(a3.getId());
        assertThat(page0.getData().get(1).getAttemptId()).isEqualTo(a2.getId());

        assertThat(page1.getData()).hasSize(1);
        assertThat(page1.getData().get(0).getAttemptId()).isEqualTo(a1.getId());
    }

    @Test
    @DisplayName("Cross-student isolation: Học viên chỉ thấy quiz attempts của chính mình trong danh sách phân trang")
    void testCrossStudentIsolation_StudentOnlySeesOwnAttempts() {
        LocalDateTime now = LocalDateTime.now();
        QuizAttempt aA1 = createAttempt(studentA, 1, 80.0, now.minusHours(3));
        QuizAttempt aA2 = createAttempt(studentA, 2, 90.0, now.minusHours(1));

        QuizAttempt aB1 = createAttempt(studentB, 1, 60.0, now.minusHours(2));
        QuizAttempt aB2 = createAttempt(studentB, 2, 85.0, now.minusMinutes(30));

        // 1. Authenticate as Student A
        authenticateAs(studentA);
        PageResponse<QuizAttemptSummaryRes> pageA = quizLearningService.getMyAttempts(PageRequest.of(0, 10));

        assertThat(pageA.getTotalElements()).isEqualTo(2);
        List<Long> studentAAttemptIds = pageA.getData().stream().map(QuizAttemptSummaryRes::getAttemptId).toList();
        assertThat(studentAAttemptIds).containsExactly(aA2.getId(), aA1.getId());
        assertThat(studentAAttemptIds).doesNotContain(aB1.getId(), aB2.getId());

        // 2. Authenticate as Student B
        authenticateAs(studentB);
        PageResponse<QuizAttemptSummaryRes> pageB = quizLearningService.getMyAttempts(PageRequest.of(0, 10));

        assertThat(pageB.getTotalElements()).isEqualTo(2);
        List<Long> studentBAttemptIds = pageB.getData().stream().map(QuizAttemptSummaryRes::getAttemptId).toList();
        assertThat(studentBAttemptIds).containsExactly(aB2.getId(), aB1.getId());
        assertThat(studentBAttemptIds).doesNotContain(aA1.getId(), aA2.getId());
    }
}
