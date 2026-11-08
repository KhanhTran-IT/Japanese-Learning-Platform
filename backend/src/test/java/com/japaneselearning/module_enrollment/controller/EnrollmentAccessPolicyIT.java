package com.japaneselearning.module_enrollment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_learning.dto.ProgressUpdateReq;
import com.japaneselearning.module_learning.repository.LessonProgressRepository;
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderItemRepository;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_payment.repository.PaymentRepository;
import com.japaneselearning.module_quiz.entity.Answer;
import com.japaneselearning.module_quiz.entity.Question;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.enums.QuestionType;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.AnswerRepository;
import com.japaneselearning.module_quiz.repository.QuestionRepository;
import com.japaneselearning.module_quiz.repository.QuizAttemptAnswerRepository;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
import com.japaneselearning.module_quiz.repository.QuizRepository;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.repository.RoleRepository;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EnrollmentAccessPolicyIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSectionRepository sectionRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository progressRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private QuizAttemptAnswerRepository attemptAnswerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private com.japaneselearning.module_leaderboard.repository.LearningActivityRepository activityRepository;

    @Autowired
    private com.japaneselearning.module_leaderboard.repository.UserScoreRepository scoreRepository;

    private User student;
    private Course paidCourse;
    private Course freeCourse;
    private CourseSection section;
    private Lesson regularLesson;
    private Lesson previewLesson;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        // FK-aware teardown
        attemptAnswerRepository.deleteAll();
        attemptRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        activityRepository.deleteAll();
        scoreRepository.deleteAll();
        progressRepository.deleteAll();
        paymentRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.STUDENT).description("Student").build()));
        Role teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.TEACHER).description("Teacher").build()));

        student = userRepository.save(User.builder()
                .email("student@example.com")
                .passwordHash("hashed-pw")
                .fullName("Test Student")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(studentRole))
                .build());

        User teacher = userRepository.save(User.builder()
                .email("teacher@example.com")
                .passwordHash("hashed-pw")
                .fullName("Test Teacher")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(teacherRole))
                .build());

        paidCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("N5 Paid Master Course")
                .slug("n5-paid-master")
                .shortDescription("Paid master course")
                .description("Detailed description")
                .level(CourseLevel.N5)
                .courseType(CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(500000))
                .salePrice(BigDecimal.valueOf(500000))
                .status(CourseStatus.PUBLISHED)
                .totalLessons(2)
                .build());

        freeCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Hiragana Free Starter")
                .slug("hiragana-free-starter")
                .shortDescription("Free starter course")
                .description("Detailed description")
                .level(CourseLevel.N5)
                .courseType(CourseType.FREE)
                .originalPrice(BigDecimal.ZERO)
                .salePrice(BigDecimal.ZERO)
                .status(CourseStatus.PUBLISHED)
                .totalLessons(1)
                .build());

        section = sectionRepository.save(CourseSection.builder()
                .course(paidCourse)
                .title("Section 1: Basics")
                .sortOrder(1)
                .status(CourseStatus.PUBLISHED)
                .build());

        regularLesson = lessonRepository.save(Lesson.builder()
                .course(paidCourse)
                .section(section)
                .title("Lesson 1: Grammar")
                .slug("lesson-1-grammar")
                .content("Grammar content")
                .videoUrl("https://video.example.com/1")
                .sortOrder(1)
                .durationMinutes(10)
                .isPreview(false)
                .status(CourseStatus.PUBLISHED)
                .build());

        previewLesson = lessonRepository.save(Lesson.builder()
                .course(paidCourse)
                .section(section)
                .title("Lesson 2: Preview Vocab")
                .slug("lesson-2-preview-vocab")
                .content("Vocab preview content")
                .videoUrl("https://video.example.com/2")
                .sortOrder(2)
                .durationMinutes(5)
                .isPreview(true)
                .status(CourseStatus.PUBLISHED)
                .build());

        quiz = quizRepository.save(Quiz.builder()
                .lesson(regularLesson)
                .course(paidCourse)
                .title("Quiz for Lesson 1")
                .passingScore(BigDecimal.valueOf(80))
                .timeLimitMinutes(15)
                .status(QuizStatus.PUBLISHED)
                .build());

        Question question = questionRepository.save(Question.builder()
                .quiz(quiz)
                .content("Question 1")
                .questionType(QuestionType.SINGLE_CHOICE)
                .points(BigDecimal.valueOf(10))
                .sortOrder(1)
                .build());

        answerRepository.save(Answer.builder()
                .question(question)
                .content("Correct Answer")
                .isCorrect(true)
                .sortOrder(1)
                .build());
    }

    private CourseEnrollment createEnrollment(Course course, EnrollmentStatus status) {
        return enrollmentRepository.save(CourseEnrollment.builder()
                .user(student)
                .course(course)
                .status(status)
                .progressPercent(status == EnrollmentStatus.COMPLETED ? 100 : 25)
                .enrolledAt(LocalDateTime.now())
                .build());
    }

    // =========================================================================
    // 1. Dashboard Visibility Tests
    // =========================================================================
    @Nested
    @DisplayName("Dashboard Visibility Tests across Enrollment States")
    class DashboardVisibilityTests {

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void dashboard_ActiveEnrollment_IsVisibleInMyCoursesAndOverview() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.ACTIVE);

            mockMvc.perform(get("/api/users/me/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result", hasSize(1)))
                    .andExpect(jsonPath("$.result[0].courseId").value(paidCourse.getId()))
                    .andExpect(jsonPath("$.result[0].courseName").value("N5 Paid Master Course"));

            mockMvc.perform(get("/api/users/me/progress"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.totalEnrolledCourses").value(1));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void dashboard_CompletedEnrollment_IsVisibleInMyCoursesAndOverview() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.COMPLETED);

            mockMvc.perform(get("/api/users/me/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result", hasSize(1)))
                    .andExpect(jsonPath("$.result[0].courseId").value(paidCourse.getId()));

            mockMvc.perform(get("/api/users/me/progress"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.totalEnrolledCourses").value(1));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void dashboard_PausedEnrollment_IsNotVisibleInMyCoursesOrOverview() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.PAUSED);

            mockMvc.perform(get("/api/users/me/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result", hasSize(0)));

            mockMvc.perform(get("/api/users/me/progress"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.totalEnrolledCourses").value(0));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void dashboard_CancelledEnrollment_IsNotVisibleInMyCoursesOrOverview() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.CANCELLED);

            mockMvc.perform(get("/api/users/me/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result", hasSize(0)));

            mockMvc.perform(get("/api/users/me/progress"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.totalEnrolledCourses").value(0));
        }
    }

    // =========================================================================
    // 2. Course Detail State (isEnrolled) Tests
    // =========================================================================
    @Nested
    @DisplayName("Public Course Detail State (isEnrolled) across Enrollment States")
    class CourseDetailStateTests {

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void courseDetail_ActiveEnrollment_ReturnsIsEnrolledTrue() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.ACTIVE);

            mockMvc.perform(get("/api/v1/courses/" + paidCourse.getSlug()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.isEnrolled").value(true));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void courseDetail_CompletedEnrollment_ReturnsIsEnrolledTrue() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.COMPLETED);

            mockMvc.perform(get("/api/v1/courses/" + paidCourse.getSlug()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.isEnrolled").value(true));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void courseDetail_PausedEnrollment_ReturnsIsEnrolledFalse() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.PAUSED);

            mockMvc.perform(get("/api/v1/courses/" + paidCourse.getSlug()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.isEnrolled").value(false));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void courseDetail_CancelledEnrollment_ReturnsIsEnrolledFalse() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.CANCELLED);

            mockMvc.perform(get("/api/v1/courses/" + paidCourse.getSlug()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.isEnrolled").value(false));
        }
    }

    // =========================================================================
    // 3. Lesson Access & Progress Tests
    // =========================================================================
    @Nested
    @DisplayName("Lesson Access & Progress across Enrollment States")
    class LessonAccessTests {

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void lessonAccess_ActiveEnrollment_AllowsAccessAndProgress() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.ACTIVE);

            mockMvc.perform(get("/api/v1/lessons/" + regularLesson.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.id").value(regularLesson.getId()));

            ProgressUpdateReq req = new ProgressUpdateReq();
            req.setWatchedPercent(80.0);
            req.setIsCompleted(true);

            mockMvc.perform(post("/api/v1/lessons/" + regularLesson.getId() + "/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void lessonAccess_CompletedEnrollment_AllowsAccessAndProgress() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.COMPLETED);

            mockMvc.perform(get("/api/v1/lessons/" + regularLesson.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.id").value(regularLesson.getId()));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void lessonAccess_PausedEnrollment_Returns403ForNonPreview() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.PAUSED);

            mockMvc.perform(get("/api/v1/lessons/" + regularLesson.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));

            ProgressUpdateReq req = new ProgressUpdateReq();
            req.setWatchedPercent(10.0);
            req.setIsCompleted(false);

            mockMvc.perform(post("/api/v1/lessons/" + regularLesson.getId() + "/progress")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void lessonAccess_CancelledEnrollment_Returns403ForNonPreview() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.CANCELLED);

            mockMvc.perform(get("/api/v1/lessons/" + regularLesson.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void lessonAccess_PreviewLesson_AllowsAccessEvenWhenPausedOrCancelled() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.CANCELLED);

            mockMvc.perform(get("/api/v1/lessons/" + previewLesson.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.id").value(previewLesson.getId()));
        }
    }

    // =========================================================================
    // 4. Quiz Access Tests
    // =========================================================================
    @Nested
    @DisplayName("Quiz Access across Enrollment States")
    class QuizAccessTests {

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void quizAccess_ActiveEnrollment_AllowsQuizzesAndStart() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.ACTIVE);

            mockMvc.perform(get("/api/v1/lessons/" + regularLesson.getId() + "/quizzes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result", hasSize(1)));

            mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.attemptId").isNumber());
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void quizAccess_CompletedEnrollment_AllowsQuizzesAndStart() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.COMPLETED);

            mockMvc.perform(get("/api/v1/lessons/" + regularLesson.getId() + "/quizzes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result", hasSize(1)));

            mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void quizAccess_PausedEnrollment_Returns403() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.PAUSED);

            mockMvc.perform(get("/api/v1/lessons/" + regularLesson.getId() + "/quizzes"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));

            mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/start"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void quizAccess_CancelledEnrollment_Returns403() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.CANCELLED);

            mockMvc.perform(get("/api/v1/lessons/" + regularLesson.getId() + "/quizzes"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));

            mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/start"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));
        }
    }

    // =========================================================================
    // 5. Historical Record Preservation & Reactivation Workflow
    // =========================================================================
    @Nested
    @DisplayName("Historical Record Preservation & Reactivation Workflow")
    class HistoricalPreservationWorkflowTests {

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void orderCreation_ActiveOrCompleted_ReturnsConflict() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.ACTIVE);

            OrderCreateReq req = new OrderCreateReq();
            req.setCourseId(paidCourse.getId());

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_ENROLLED.getCode()));
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void orderCreation_CancelledEnrollment_AllowsNewOrder() throws Exception {
            createEnrollment(paidCourse, EnrollmentStatus.CANCELLED);

            OrderCreateReq req = new OrderCreateReq();
            req.setCourseId(paidCourse.getId());

            mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.orderCode").isNotEmpty());
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void paymentSimulation_ReactivatesCancelledEnrollment_PreservesRecordId() throws Exception {
            CourseEnrollment historicalRecord = createEnrollment(paidCourse, EnrollmentStatus.CANCELLED);
            Long originalEnrollmentId = historicalRecord.getId();

            // Create order
            OrderCreateReq req = new OrderCreateReq();
            req.setCourseId(paidCourse.getId());
            String orderResJson = mockMvc.perform(post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(orderResJson).path("result").path("id").asLong();

            // Simulate payment
            mockMvc.perform(post("/api/v1/payments/simulate/" + orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000));

            // Verify historical enrollment record was NOT deleted or duplicated, but updated in-place to ACTIVE
            List<CourseEnrollment> enrollments = enrollmentRepository.findAll();
            assertThat(enrollments).hasSize(1);

            CourseEnrollment updated = enrollments.get(0);
            assertThat(updated.getId()).isEqualTo(originalEnrollmentId);
            assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        }

        @Test
        @WithMockUser(username = "student@example.com", roles = "STUDENT")
        void freeCourse_ReactivatesCancelledEnrollment_WithoutUniqueConstraintError() throws Exception {
            CourseEnrollment historicalRecord = createEnrollment(freeCourse, EnrollmentStatus.CANCELLED);
            Long originalEnrollmentId = historicalRecord.getId();

            // Re-enroll in free course
            mockMvc.perform(post("/api/v1/courses/" + freeCourse.getId() + "/enroll"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000));

            List<CourseEnrollment> enrollments = enrollmentRepository.findAll();
            assertThat(enrollments).hasSize(1);

            CourseEnrollment updated = enrollments.get(0);
            assertThat(updated.getId()).isEqualTo(originalEnrollmentId);
            assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        }
    }
}
