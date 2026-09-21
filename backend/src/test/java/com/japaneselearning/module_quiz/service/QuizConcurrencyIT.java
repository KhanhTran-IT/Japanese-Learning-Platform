package com.japaneselearning.module_quiz.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
import com.japaneselearning.module_quiz.repository.QuizRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class QuizConcurrencyIT {

    @Autowired
    private QuizLearningService quizLearningService;

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
    private Quiz testQuiz;

    @BeforeEach
    void setUp() {
        // Clean up before starting
        attemptRepository.deleteAll();
        quizRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create a user (student)
        testUser = User.builder()
                .email("concurrency.test@example.com")
                .passwordHash("password")
                .fullName("Concurrency Test User")
                .status(UserStatus.ACTIVE)
                .build();
        testUser = userRepository.saveAndFlush(testUser);

        // 1b. Create a teacher
        User teacher = User.builder()
                .email("teacher.test@example.com")
                .passwordHash("password")
                .fullName("Concurrency Teacher")
                .status(UserStatus.ACTIVE)
                .build();
        teacher = userRepository.saveAndFlush(teacher);

        // 2. Create a course
        Course course = Course.builder()
                .title("Test Course")
                .slug("test-course")
                .teacher(teacher)
                .level(CourseLevel.N5)
                .courseType(CourseType.FREE)
                .status(CourseStatus.PUBLISHED)
                .build();
        course = courseRepository.saveAndFlush(course);

        // 3. Enroll the user
        CourseEnrollment enrollment = CourseEnrollment.builder()
                .user(testUser)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.saveAndFlush(enrollment);

        // 4. Create a published quiz with maxAttempts = 2
        testQuiz = Quiz.builder()
                .course(course)
                .title("Concurrency Test Quiz")
                .status(QuizStatus.PUBLISHED)
                .maxAttempts(2)
                .build();
        testQuiz = quizRepository.saveAndFlush(testQuiz);
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
    void startAttempt_ShouldNotExceedMaxAttempts_UnderHighConcurrency() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger exceptionCount = new AtomicInteger();

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    // Set up security context for this thread
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            testUser.getEmail(), null, Collections.emptyList());
                    context.setAuthentication(auth);
                    SecurityContextHolder.setContext(context);

                    // Wait for all threads to be ready
                    startLatch.await();

                    // Try to start attempt
                    quizLearningService.startAttempt(testQuiz.getId());
                    successCount.incrementAndGet();
                } catch (AppException e) {
                    if (e.getErrorCode() == ErrorCode.QUIZ_MAX_ATTEMPTS_REACHED) {
                        exceptionCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // If DataIntegrityViolationException is somehow not mapped (it shouldn't be if we mapped it)
                    // We can also count it.
                    e.printStackTrace();
                } finally {
                    SecurityContextHolder.clearContext();
                    latch.countDown();
                }
            });
        }

        // Fire all threads at once
        startLatch.countDown();
        // Wait for all threads to finish
        latch.await();
        executorService.shutdown();

        // With maxAttempts = 2 and no retry loop in the service (since the TX is rollback-only on constraint violation),
        // concurrent requests will compete for attempt_number 1 (and possibly 2 if slightly staggered).
        // The most important thing is that success count NEVER exceeds 2.
        int successes = successCount.get();
        org.junit.jupiter.api.Assertions.assertTrue(successes >= 1 && successes <= 2,
                "Success count should be between 1 and 2, but was: " + successes);

        // The rest should have failed
        assertEquals(numberOfThreads - successes, exceptionCount.get(), "Remaining attempts should have failed");

        // Verify database actually matches success count
        long actualCountInDb = attemptRepository.countByUserIdAndQuizId(testUser.getId(), testQuiz.getId());
        assertEquals(successes, actualCountInDb);
    }
}
