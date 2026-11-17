package com.japaneselearning.module_quiz.repository;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.entity.QuizAttempt;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class QuizAttemptRepositoryTest {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("repo-test-" + System.currentTimeMillis() + "@example.com")
                .passwordHash("password")
                .fullName("Repo Test User")
                .status(UserStatus.ACTIVE)
                .build();
        em.persist(user);

        Course course = Course.builder()
                .title("Repo Test Course")
                .slug("repo-test-course-" + System.currentTimeMillis())
                .teacher(user)
                .level(CourseLevel.N5)
                .courseType(CourseType.FREE)
                .status(CourseStatus.PUBLISHED)
                .build();
        em.persist(course);

        quiz = Quiz.builder()
                .course(course)
                .title("Repo Test Quiz")
                .status(QuizStatus.PUBLISHED)
                .timeLimitMinutes(15)
                .build();
        em.persist(quiz);
        em.flush();
    }

    private QuizAttempt createAttempt(int attemptNumber, QuizAttemptStatus status, LocalDateTime startedAt, LocalDateTime expiresAt) {
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .attemptNumber(attemptNumber)
                .status(status)
                .startedAt(startedAt)
                .expiresAt(expiresAt)
                .build();
        em.persist(attempt);
        return attempt;
    }

    @Test
    @DisplayName("findExpiredAttemptIds: Returns only IN_PROGRESS attempts with expiresAt <= cutoff ordered by expiresAt ASC, id ASC")
    void findExpiredAttemptIds_ReturnsMatchingAttemptsInOrder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(5);

        // 1. Expired 15 mins ago (expiresAt <= cutoff) -> Should match
        QuizAttempt expiredOlder = createAttempt(1, QuizAttemptStatus.IN_PROGRESS, now.minusMinutes(30), now.minusMinutes(15));
        // 2. Expired 6 mins ago (expiresAt <= cutoff) -> Should match
        QuizAttempt expiredNewer = createAttempt(2, QuizAttemptStatus.IN_PROGRESS, now.minusMinutes(21), now.minusMinutes(6));
        // 3. Expired 2 mins ago (cutoff is 5 mins ago, so within grace period) -> Should NOT match
        QuizAttempt withinGrace = createAttempt(3, QuizAttemptStatus.IN_PROGRESS, now.minusMinutes(17), now.minusMinutes(2));
        // 4. Expires in the future -> Should NOT match
        QuizAttempt futureAttempt = createAttempt(4, QuizAttemptStatus.IN_PROGRESS, now, now.plusMinutes(15));
        // 5. Untimed (expiresAt is null) -> Should NOT match
        QuizAttempt untimed = createAttempt(5, QuizAttemptStatus.IN_PROGRESS, now.minusHours(2), null);
        // 6. Already SUBMITTED even if deadline passed -> Should NOT match
        QuizAttempt submitted = createAttempt(6, QuizAttemptStatus.SUBMITTED, now.minusMinutes(30), now.minusMinutes(15));
        // 7. Already EXPIRED -> Should NOT match
        QuizAttempt alreadyExpired = createAttempt(7, QuizAttemptStatus.EXPIRED, now.minusMinutes(30), now.minusMinutes(15));

        em.flush();

        List<Long> matchedIds = quizAttemptRepository.findExpiredAttemptIds(
                QuizAttemptStatus.IN_PROGRESS,
                cutoff,
                PageRequest.of(0, 10)
        );

        assertThat(matchedIds).containsExactly(expiredOlder.getId(), expiredNewer.getId());
    }

    @Test
    @DisplayName("findExpiredAttemptIds: Respects bounded pagination limit")
    void findExpiredAttemptIds_RespectsBoundedPagination() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(5);

        QuizAttempt a1 = createAttempt(1, QuizAttemptStatus.IN_PROGRESS, now.minusMinutes(30), now.minusMinutes(15));
        QuizAttempt a2 = createAttempt(2, QuizAttemptStatus.IN_PROGRESS, now.minusMinutes(25), now.minusMinutes(10));
        QuizAttempt a3 = createAttempt(3, QuizAttemptStatus.IN_PROGRESS, now.minusMinutes(22), now.minusMinutes(7));
        em.flush();

        List<Long> batch1 = quizAttemptRepository.findExpiredAttemptIds(
                QuizAttemptStatus.IN_PROGRESS,
                cutoff,
                PageRequest.of(0, 2)
        );

        assertThat(batch1).hasSize(2);
        assertThat(batch1).containsExactly(a1.getId(), a2.getId());

        List<Long> batch2 = quizAttemptRepository.findExpiredAttemptIds(
                QuizAttemptStatus.IN_PROGRESS,
                cutoff,
                PageRequest.of(1, 2)
        );

        assertThat(batch2).hasSize(1);
        assertThat(batch2).containsExactly(a3.getId());
    }

    @Test
    @DisplayName("markAttemptsExpiredByIds: Atomically transitions matching attempts and ignores non-IN_PROGRESS")
    void markAttemptsExpiredByIds_TransitionsOnlyInProgress() {
        LocalDateTime now = LocalDateTime.now();

        QuizAttempt a1 = createAttempt(1, QuizAttemptStatus.IN_PROGRESS, now.minusMinutes(30), now.minusMinutes(15));
        QuizAttempt a2 = createAttempt(2, QuizAttemptStatus.IN_PROGRESS, now.minusMinutes(25), now.minusMinutes(10));
        QuizAttempt a3 = createAttempt(3, QuizAttemptStatus.SUBMITTED, now.minusMinutes(30), now.minusMinutes(15));
        em.flush();

        int updatedCount = quizAttemptRepository.markAttemptsExpiredByIds(
                List.of(a1.getId(), a2.getId(), a3.getId()),
                QuizAttemptStatus.EXPIRED,
                QuizAttemptStatus.IN_PROGRESS
        );

        assertThat(updatedCount).isEqualTo(2);

        em.clear();

        QuizAttempt reloadedA1 = quizAttemptRepository.findById(a1.getId()).orElseThrow();
        QuizAttempt reloadedA2 = quizAttemptRepository.findById(a2.getId()).orElseThrow();
        QuizAttempt reloadedA3 = quizAttemptRepository.findById(a3.getId()).orElseThrow();

        assertThat(reloadedA1.getStatus()).isEqualTo(QuizAttemptStatus.EXPIRED);
        assertThat(reloadedA2.getStatus()).isEqualTo(QuizAttemptStatus.EXPIRED);
        assertThat(reloadedA3.getStatus()).isEqualTo(QuizAttemptStatus.SUBMITTED);

        // Calling again on the same IDs (simulating another concurrent instance) updates 0 rows
        int secondRunCount = quizAttemptRepository.markAttemptsExpiredByIds(
                List.of(a1.getId(), a2.getId(), a3.getId()),
                QuizAttemptStatus.EXPIRED,
                QuizAttemptStatus.IN_PROGRESS
        );
        assertThat(secondRunCount).isEqualTo(0);
    }
}
