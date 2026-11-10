package com.japaneselearning.module_course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.dto.CourseCreateReq;
import com.japaneselearning.module_course.dto.CourseUpdateReq;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_course.repository.LessonResourceRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_learning.repository.LessonProgressRepository;
import com.japaneselearning.module_order.repository.OrderItemRepository;
import com.japaneselearning.module_order.repository.OrderRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CoursePublishValidationIT {

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
    private LessonResourceRepository resourceRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository progressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

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

    private User teacherA;
    private User teacherB;

    @BeforeEach
    void setUp() {
        attemptAnswerRepository.deleteAll();
        attemptRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        resourceRepository.deleteAll();
        progressRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        Role teacherRole = roleRepository.findByName(RoleName.TEACHER).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleName.TEACHER).description("Teacher").build()));

        teacherA = userRepository.save(User.builder()
                .email("teacher_a@example.com")
                .fullName("Teacher A")
                .passwordHash("$2a$10$dummyHashForTestingPurposesOnly12345678901234567890")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(teacherRole))
                .build());

        teacherB = userRepository.save(User.builder()
                .email("teacher_b@example.com")
                .fullName("Teacher B")
                .passwordHash("$2a$10$dummyHashForTestingPurposesOnly12345678901234567890")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(teacherRole))
                .build());
    }

    // =========================================================================
    // 1. PRICING INVARIANTS ON CREATE (HTTP)
    // =========================================================================
    @Nested
    @DisplayName("HTTP Course Creation Pricing Tests")
    class CreatePricingEndpointTests {

        @Test
        @DisplayName("POST course with FREE type and positive price returns 400 COURSE_FREE_PRICE_INVALID")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void createCourse_FreeWithPrice_ReturnsBadRequest() throws Exception {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Free Japanese N5")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .originalPrice(BigDecimal.valueOf(200000))
                    .build();

            mockMvc.perform(post("/api/v1/admin/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_FREE_PRICE_INVALID.getCode()));
        }

        @Test
        @DisplayName("POST course with PAID type and null originalPrice returns 400 COURSE_PRICE_REQUIRED")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void createCourse_PaidNullPrice_ReturnsBadRequest() throws Exception {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Paid Japanese N4")
                    .level(CourseLevel.N4)
                    .courseType(CourseType.PAID)
                    .originalPrice(null)
                    .build();

            mockMvc.perform(post("/api/v1/admin/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_PRICE_REQUIRED.getCode()));
        }

        @Test
        @DisplayName("POST course with PAID type and salePrice > originalPrice returns 400 COURSE_SALE_PRICE_INVALID")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void createCourse_PaidSalePriceHigher_ReturnsBadRequest() throws Exception {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Paid Japanese N4")
                    .level(CourseLevel.N4)
                    .courseType(CourseType.PAID)
                    .originalPrice(BigDecimal.valueOf(500000))
                    .salePrice(BigDecimal.valueOf(600000))
                    .build();

            mockMvc.perform(post("/api/v1/admin/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_SALE_PRICE_INVALID.getCode()));
        }

        @Test
        @DisplayName("POST valid PAID course returns 200 OK")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void createCourse_ValidPaid_ReturnsOk() throws Exception {
            CourseCreateReq req = CourseCreateReq.builder()
                    .title("Paid Japanese N4 Master")
                    .level(CourseLevel.N4)
                    .courseType(CourseType.PAID)
                    .originalPrice(BigDecimal.valueOf(500000))
                    .salePrice(BigDecimal.valueOf(350000))
                    .build();

            mockMvc.perform(post("/api/v1/admin/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.courseType").value("PAID"))
                    .andExpect(jsonPath("$.result.originalPrice").value(500000))
                    .andExpect(jsonPath("$.result.salePrice").value(350000))
                    .andExpect(jsonPath("$.result.status").value("DRAFT"));
        }
    }

    // =========================================================================
    // 2. PUBLISH ENDPOINT CURRICULUM VALIDATION
    // =========================================================================
    @Nested
    @DisplayName("HTTP Publish Course Endpoint Tests")
    class PublishEndpointTests {

        @Test
        @DisplayName("Publishing empty course returns 400 COURSE_CANNOT_PUBLISH_EMPTY")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void publishCourse_EmptyCourse_ReturnsBadRequest() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Empty Course")
                    .slug("empty-course")
                    .teacher(teacherA)
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build());

            mockMvc.perform(put("/api/v1/admin/courses/" + course.getId() + "/publish"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_CANNOT_PUBLISH_EMPTY.getCode()));
        }

        @Test
        @DisplayName("Publishing course with only draft section and draft lesson returns 400 COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void publishCourse_DraftCurriculum_ReturnsBadRequest() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Course with draft curriculum")
                    .slug("course-draft-curriculum")
                    .teacher(teacherA)
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build());

            CourseSection section = sectionRepository.save(CourseSection.builder()
                    .course(course)
                    .title("Draft Section")
                    .sortOrder(1)
                    .status(CourseStatus.DRAFT)
                    .build());

            lessonRepository.save(Lesson.builder()
                    .course(course)
                    .section(section)
                    .title("Draft Lesson")
                    .slug("draft-lesson")
                    .sortOrder(1)
                    .status(CourseStatus.DRAFT)
                    .build());

            mockMvc.perform(put("/api/v1/admin/courses/" + course.getId() + "/publish"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS.getCode()));
        }

        @Test
        @DisplayName("Publishing course with published section and published lesson succeeds with 200")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void publishCourse_ValidPublishedCurriculum_ReturnsOk() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Ready to publish course")
                    .slug("ready-to-publish-course")
                    .teacher(teacherA)
                    .level(CourseLevel.N5)
                    .courseType(CourseType.PAID)
                    .originalPrice(BigDecimal.valueOf(500000))
                    .salePrice(BigDecimal.valueOf(350000))
                    .status(CourseStatus.DRAFT)
                    .build());

            CourseSection section = sectionRepository.save(CourseSection.builder()
                    .course(course)
                    .title("Published Section")
                    .sortOrder(1)
                    .status(CourseStatus.PUBLISHED)
                    .build());

            lessonRepository.save(Lesson.builder()
                    .course(course)
                    .section(section)
                    .title("Published Lesson")
                    .slug("published-lesson")
                    .sortOrder(1)
                    .status(CourseStatus.PUBLISHED)
                    .build());

            mockMvc.perform(put("/api/v1/admin/courses/" + course.getId() + "/publish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.status").value("PUBLISHED"));
        }
    }

    // =========================================================================
    // 3. PREVENT UPDATE FROM BYPASSING PUBLICATION VALIDATION
    // =========================================================================
    @Nested
    @DisplayName("Prevent Update Request from Bypassing Publication Validation")
    class UpdateBypassPreventionTests {

        @Test
        @DisplayName("PUT /courses/{id} with status=PUBLISHED on draft empty course returns 400 COURSE_CANNOT_PUBLISH_EMPTY")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void updateCourse_BypassWithEmptyCurriculum_ReturnsBadRequest() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Draft Empty Course")
                    .slug("draft-empty-course")
                    .teacher(teacherA)
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build());

            CourseUpdateReq req = CourseUpdateReq.builder()
                    .title("Hacked Publish")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.PUBLISHED)
                    .build();

            mockMvc.perform(put("/api/v1/admin/courses/" + course.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_CANNOT_PUBLISH_EMPTY.getCode()));
        }

        @Test
        @DisplayName("PUT /courses/{id} with invalid state transition PUBLISHED -> DRAFT returns 400 COURSE_INVALID_STATUS_TRANSITION")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void updateCourse_PublishedToDraft_ReturnsBadRequest() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Published Course")
                    .slug("published-course")
                    .teacher(teacherA)
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.PUBLISHED)
                    .build());

            CourseUpdateReq req = CourseUpdateReq.builder()
                    .title("Published Course")
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build();

            mockMvc.perform(put("/api/v1/admin/courses/" + course.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_INVALID_STATUS_TRANSITION.getCode()));
        }

        @Test
        @DisplayName("PUT /courses/{id}/hide on DRAFT course returns 400 COURSE_INVALID_STATUS_TRANSITION")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void hideCourse_DraftCourse_ReturnsBadRequest() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Draft Course")
                    .slug("draft-course")
                    .teacher(teacherA)
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build());

            mockMvc.perform(put("/api/v1/admin/courses/" + course.getId() + "/hide"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_INVALID_STATUS_TRANSITION.getCode()));
        }

        @Test
        @DisplayName("PUT /courses/{id}/hide on PUBLISHED course succeeds and sets status to HIDDEN")
        @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
        void hideCourse_PublishedCourse_ReturnsOk() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Published Course To Hide")
                    .slug("published-course-to-hide")
                    .teacher(teacherA)
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.PUBLISHED)
                    .build());

            mockMvc.perform(put("/api/v1/admin/courses/" + course.getId() + "/hide"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.status").value("HIDDEN"));
        }
    }

    // =========================================================================
    // 4. TEACHER ISOLATION ON NEW VALIDATION LOGIC
    // =========================================================================
    @Nested
    @DisplayName("Teacher Isolation Enforcement on Publishing Endpoints")
    class TeacherIsolationEndpointTests {

        @Test
        @DisplayName("Teacher B cannot publish Teacher A's course (403 DATA_ISOLATION_FORBIDDEN)")
        @WithMockUser(username = "teacher_b@example.com", roles = "TEACHER")
        void teacherCannotPublishOtherTeacherCourse() throws Exception {
            Course course = courseRepository.save(Course.builder()
                    .title("Teacher A's Course")
                    .slug("teacher-a-course")
                    .teacher(teacherA)
                    .level(CourseLevel.N5)
                    .courseType(CourseType.FREE)
                    .status(CourseStatus.DRAFT)
                    .build());

            mockMvc.perform(put("/api/v1/admin/courses/" + course.getId() + "/publish"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
        }
    }
}
