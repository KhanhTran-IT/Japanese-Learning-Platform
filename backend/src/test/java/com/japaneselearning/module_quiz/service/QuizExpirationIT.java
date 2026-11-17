package com.japaneselearning.module_quiz.service;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.entity.QuizAttempt;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
import com.japaneselearning.module_quiz.repository.QuizRepository;
import com.japaneselearning.module_quiz.scheduler.QuizAttemptScheduler;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class QuizExpirationIT {

    @Autowired
    private QuizAttemptScheduler scheduler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    private User testUser;
    private Quiz timedQuiz;
    private Quiz untimedQuiz;

    @BeforeEach
    void setUp() {
        attemptRepository.deleteAll();
        quizRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create a user
        testUser = User.builder()
                .email("expiration.test@example.com")
                .passwordHash("password")
                .fullName("Expiration Test User")
                .status(UserStatus.ACTIVE)
                .build();
        testUser = userRepository.saveAndFlush(testUser);

        // 2. Create a course
        Course course = Course.builder()
                .title("Expiration Course")
                .slug("expiration-course")
                .teacher(testUser)
                .level(CourseLevel.N5)
                .courseType(CourseType.FREE)
                .status(CourseStatus.PUBLISHED)
                .build();
        course = courseRepository.saveAndFlush(course);

        // 3. Create a timed quiz (10 minutes)
        timedQuiz = Quiz.builder()
                .course(course)
                .title("Timed Quiz")
                .status(QuizStatus.PUBLISHED)
                .maxAttempts(50)
                .timeLimitMinutes(10)
                .build();
        timedQuiz = quizRepository.saveAndFlush(timedQuiz);

        // 4. Create an untimed quiz
        untimedQuiz = Quiz.builder()
                .course(course)
                .title("Untimed Quiz")
                .status(QuizStatus.PUBLISHED)
                .maxAttempts(50)
                .timeLimitMinutes(null)
                .build();
        untimedQuiz = quizRepository.saveAndFlush(untimedQuiz);
    }

    @AfterEach
    void tearDown() {
        attemptRepository.deleteAll();
        quizRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("expireStaleQuizAttempts: Marks expired attempts past grace period while keeping within-grace and untimed attempts")
    void expireStaleQuizAttempts_ShouldMarkAsExpired_WhenDeadlinePassed() {
        LocalDateTime now = LocalDateTime.now();

        // Attempt 1: Timed, Started 20 mins ago (TimeLimit=10 => expiresAt=10m ago. Grace=5m => Cutoff=5m ago. Expired!)
        LocalDateTime startedAt1 = now.minusMinutes(20);
        QuizAttempt attempt1 = QuizAttempt.builder()
                .user(testUser)
                .quiz(timedQuiz)
                .attemptNumber(1)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(startedAt1)
                .expiresAt(startedAt1.plusMinutes(10))
                .build();
        attempt1 = attemptRepository.saveAndFlush(attempt1);

        // Attempt 2: Timed, Started 12 mins ago (TimeLimit=10 => expiresAt=2m ago. Cutoff=5m ago => within grace period!)
        LocalDateTime startedAt2 = now.minusMinutes(12);
        QuizAttempt attempt2 = QuizAttempt.builder()
                .user(testUser)
                .quiz(timedQuiz)
                .attemptNumber(2)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(startedAt2)
                .expiresAt(startedAt2.plusMinutes(10))
                .build();
        attempt2 = attemptRepository.saveAndFlush(attempt2);

        // Attempt 3: Untimed, Started 1 day ago (Should never expire via this scheduler)
        QuizAttempt attempt3 = QuizAttempt.builder()
                .user(testUser)
                .quiz(untimedQuiz)
                .attemptNumber(1)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(now.minusDays(1))
                .expiresAt(null)
                .build();
        attempt3 = attemptRepository.saveAndFlush(attempt3);

        // Run scheduler manually
        int expiredCount = scheduler.expireStaleQuizAttempts();
        assertEquals(1, expiredCount, "Only attempt 1 should be expired");

        // Assertions
        Optional<QuizAttempt> updatedAttempt1 = attemptRepository.findById(attempt1.getId());
        assertEquals(QuizAttemptStatus.EXPIRED, updatedAttempt1.get().getStatus(), "Attempt 1 should be EXPIRED");

        Optional<QuizAttempt> updatedAttempt2 = attemptRepository.findById(attempt2.getId());
        assertEquals(QuizAttemptStatus.IN_PROGRESS, updatedAttempt2.get().getStatus(), "Attempt 2 should still be IN_PROGRESS");

        Optional<QuizAttempt> updatedAttempt3 = attemptRepository.findById(attempt3.getId());
        assertEquals(QuizAttemptStatus.IN_PROGRESS, updatedAttempt3.get().getStatus(), "Attempt 3 should still be IN_PROGRESS because it is untimed");
    }

    @Test
    @DisplayName("expireStaleQuizAttempts: Precise time boundary verification for grace period")
    void expireStaleQuizAttempts_TimeBoundaryVerification() {
        LocalDateTime now = LocalDateTime.now();
        // Cutoff is now - 5 minutes
        // Attempt A: expiresAt is 5 minutes 30 seconds ago (strictly <= cutoff) -> Should expire
        QuizAttempt attemptA = QuizAttempt.builder()
                .user(testUser)
                .quiz(timedQuiz)
                .attemptNumber(1)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(16))
                .expiresAt(now.minusMinutes(5).minusSeconds(30))
                .build();
        attemptA = attemptRepository.saveAndFlush(attemptA);

        // Attempt B: expiresAt is 4 minutes 30 seconds ago (> cutoff, within grace period) -> Should NOT expire
        QuizAttempt attemptB = QuizAttempt.builder()
                .user(testUser)
                .quiz(timedQuiz)
                .attemptNumber(2)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(14))
                .expiresAt(now.minusMinutes(4).minusSeconds(30))
                .build();
        attemptB = attemptRepository.saveAndFlush(attemptB);

        int expiredCount = scheduler.expireStaleQuizAttempts();
        assertEquals(1, expiredCount);

        assertEquals(QuizAttemptStatus.EXPIRED, attemptRepository.findById(attemptA.getId()).get().getStatus());
        assertEquals(QuizAttemptStatus.IN_PROGRESS, attemptRepository.findById(attemptB.getId()).get().getStatus());
    }

    @Test
    @DisplayName("expireStaleQuizAttempts: Multi-instance concurrency safe execution without duplicate work or conflict")
    void expireStaleQuizAttempts_MultiInstanceConcurrencySafety() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        // Create 10 expired attempts
        List<QuizAttempt> attempts = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            QuizAttempt a = QuizAttempt.builder()
                    .user(testUser)
                    .quiz(timedQuiz)
                    .attemptNumber(i)
                    .status(QuizAttemptStatus.IN_PROGRESS)
                    .startedAt(now.minusMinutes(30))
                    .expiresAt(now.minusMinutes(20))
                    .build();
            attempts.add(a);
        }
        attemptRepository.saveAllAndFlush(attempts);

        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        AtomicInteger totalExpiredByAllThreads = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    barrier.await();
                    int count = scheduler.expireStaleQuizAttempts();
                    totalExpiredByAllThreads.addAndGet(count);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        // Exactly 10 attempts should have been expired across all competing instances
        assertEquals(10, totalExpiredByAllThreads.get(), "Total expired count across instances must equal 10");

        // Verify in DB all 10 are now EXPIRED
        List<QuizAttempt> reloaded = attemptRepository.findAll();
        for (QuizAttempt a : reloaded) {
            assertEquals(QuizAttemptStatus.EXPIRED, a.getStatus());
        }

        // Subsequent scheduler run should find 0
        int subsequentRun = scheduler.expireStaleQuizAttempts();
        assertEquals(0, subsequentRun);
    }
}
