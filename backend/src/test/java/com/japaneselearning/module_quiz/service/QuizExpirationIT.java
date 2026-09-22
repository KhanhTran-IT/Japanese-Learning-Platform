package com.japaneselearning.module_quiz.service;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

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
                .maxAttempts(5)
                .timeLimitMinutes(10)
                .build();
        timedQuiz = quizRepository.saveAndFlush(timedQuiz);

        // 4. Create an untimed quiz
        untimedQuiz = Quiz.builder()
                .course(course)
                .title("Untimed Quiz")
                .status(QuizStatus.PUBLISHED)
                .maxAttempts(5)
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
    void expireStaleQuizAttempts_ShouldMarkAsExpired_WhenDeadlinePassed() {
        LocalDateTime now = LocalDateTime.now();

        // Attempt 1: Timed, Started 20 mins ago (TimeLimit=10, Grace=5 => Expired after 15 mins)
        QuizAttempt attempt1 = QuizAttempt.builder()
                .user(testUser)
                .quiz(timedQuiz)
                .attemptNumber(1)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(20))
                .build();
        attempt1 = attemptRepository.saveAndFlush(attempt1);

        // Attempt 2: Timed, Started 12 mins ago (Not expired yet, within grace period)
        QuizAttempt attempt2 = QuizAttempt.builder()
                .user(testUser)
                .quiz(timedQuiz)
                .attemptNumber(2)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(12))
                .build();
        attempt2 = attemptRepository.saveAndFlush(attempt2);

        // Attempt 3: Untimed, Started 1 day ago (Should never expire via this scheduler)
        QuizAttempt attempt3 = QuizAttempt.builder()
                .user(testUser)
                .quiz(untimedQuiz)
                .attemptNumber(1)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(now.minusDays(1))
                .build();
        attempt3 = attemptRepository.saveAndFlush(attempt3);

        // Run scheduler manually
        scheduler.expireStaleQuizAttempts();

        // Assertions
        Optional<QuizAttempt> updatedAttempt1 = attemptRepository.findById(attempt1.getId());
        assertEquals(QuizAttemptStatus.EXPIRED, updatedAttempt1.get().getStatus(), "Attempt 1 should be EXPIRED");

        Optional<QuizAttempt> updatedAttempt2 = attemptRepository.findById(attempt2.getId());
        assertEquals(QuizAttemptStatus.IN_PROGRESS, updatedAttempt2.get().getStatus(), "Attempt 2 should still be IN_PROGRESS");

        Optional<QuizAttempt> updatedAttempt3 = attemptRepository.findById(attempt3.getId());
        assertEquals(QuizAttemptStatus.IN_PROGRESS, updatedAttempt3.get().getStatus(), "Attempt 3 should still be IN_PROGRESS because it is untimed");
    }
}
