package com.japaneselearning.module_course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.dto.CourseUpdateReq;
import com.japaneselearning.module_course.dto.LessonCreateReq;
import com.japaneselearning.module_course.dto.LessonUpdateReq;
import com.japaneselearning.module_course.dto.ResourceCreateReq;
import com.japaneselearning.module_course.dto.ResourceUpdateReq;
import com.japaneselearning.module_course.dto.SectionCreateReq;
import com.japaneselearning.module_course.dto.SectionUpdateReq;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.entity.LessonResource;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.enums.ResourceType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_course.repository.LessonResourceRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_learning.repository.LessonProgressRepository;
import com.japaneselearning.module_order.repository.OrderItemRepository;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_quiz.dto.AnswerCreateReq;
import com.japaneselearning.module_quiz.dto.AnswerUpdateReq;
import com.japaneselearning.module_quiz.dto.QuestionCreateReq;
import com.japaneselearning.module_quiz.dto.QuestionUpdateReq;
import com.japaneselearning.module_quiz.dto.QuizCreateReq;
import com.japaneselearning.module_quiz.dto.QuizUpdateReq;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AdminTeacherIsolationIT {

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
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository progressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User teacherA;
    private User teacherB;
    private User admin;

    private Course courseA;
    private CourseSection sectionA;
    private Lesson lessonA;
    private LessonResource resourceA;
    private Quiz quizA;
    private Question questionA;
    private Answer answerA;

    private Course courseB;

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
        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleName.ADMIN).description("Admin").build()));

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

        admin = userRepository.save(User.builder()
                .email("admin@example.com")
                .fullName("System Admin")
                .passwordHash("$2a$10$dummyHashForTestingPurposesOnly12345678901234567890")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(adminRole))
                .build());

        courseA = courseRepository.save(Course.builder()
                .title("Course A by Teacher A")
                .slug("course-a-by-teacher-a")
                .teacher(teacherA)
                .level(CourseLevel.N5)
                .courseType(CourseType.FREE)
                .status(CourseStatus.DRAFT)
                .build());

        sectionA = sectionRepository.save(CourseSection.builder()
                .course(courseA)
                .title("Section A")
                .sortOrder(1)
                .status(CourseStatus.DRAFT)
                .build());

        lessonA = lessonRepository.save(Lesson.builder()
                .section(sectionA)
                .course(courseA)
                .title("Lesson A")
                .slug("lesson-a")
                .sortOrder(1)
                .status(CourseStatus.DRAFT)
                .isPreview(false)
                .durationMinutes(20)
                .build());

        resourceA = resourceRepository.save(LessonResource.builder()
                .lesson(lessonA)
                .title("Resource A")
                .resourceType(ResourceType.PDF)
                .fileUrl("https://example.com/resA.pdf")
                .sortOrder(1)
                .build());

        quizA = quizRepository.save(Quiz.builder()
                .course(courseA)
                .lesson(lessonA)
                .title("Quiz A")
                .status(QuizStatus.DRAFT)
                .timeLimitMinutes(15)
                .passingScore(BigDecimal.valueOf(80))
                .maxAttempts(3)
                .build());

        questionA = questionRepository.save(Question.builder()
                .quiz(quizA)
                .content("Question 1")
                .questionType(QuestionType.SINGLE_CHOICE)
                .points(BigDecimal.valueOf(10))
                .sortOrder(1)
                .build());

        answerA = answerRepository.save(Answer.builder()
                .question(questionA)
                .content("Answer 1")
                .isCorrect(true)
                .sortOrder(1)
                .build());

        courseB = courseRepository.save(Course.builder()
                .title("Course B by Teacher B")
                .slug("course-b-by-teacher-b")
                .teacher(teacherB)
                .level(CourseLevel.N5)
                .courseType(CourseType.FREE)
                .status(CourseStatus.DRAFT)
                .build());
    }

    // =========================================================================
    // 1. COURSE READ & WRITE ISOLATION
    // =========================================================================

    @Test
    @DisplayName("Teacher B cannot read Teacher A's course detail (403 DATA_ISOLATION_FORBIDDEN)")
    @WithMockUser(username = "teacher_b@example.com", roles = "TEACHER")
    void teacherCannotReadAnotherTeacherCourseDetail() throws Exception {
        mockMvc.perform(get("/api/v1/admin/courses/" + courseA.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("Teacher A can read own course detail (200 OK)")
    @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
    void teacherCanReadOwnCourseDetail() throws Exception {
        mockMvc.perform(get("/api/v1/admin/courses/" + courseA.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(courseA.getId()))
                .andExpect(jsonPath("$.result.title").value(courseA.getTitle()));
    }

    @Test
    @DisplayName("Admin can read any teacher's course detail (200 OK)")
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminCanReadAnyTeacherCourseDetail() throws Exception {
        mockMvc.perform(get("/api/v1/admin/courses/" + courseA.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(courseA.getId()));

        mockMvc.perform(get("/api/v1/admin/courses/" + courseB.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(courseB.getId()));
    }

    @Test
    @DisplayName("Teacher B cannot update, delete, publish, or hide Teacher A's course (403)")
    @WithMockUser(username = "teacher_b@example.com", roles = "TEACHER")
    void teacherCannotModifyAnotherTeacherCourse() throws Exception {
        CourseUpdateReq updateReq = CourseUpdateReq.builder()
                .title("Hacked Title")
                .level(CourseLevel.N1)
                .courseType(CourseType.FREE)
                .status(CourseStatus.DRAFT)
                .build();

        mockMvc.perform(put("/api/v1/admin/courses/" + courseA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(delete("/api/v1/admin/courses/" + courseA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(put("/api/v1/admin/courses/" + courseA.getId() + "/publish"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(put("/api/v1/admin/courses/" + courseA.getId() + "/hide"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
    }

    // =========================================================================
    // 2. SECTION ISOLATION
    // =========================================================================

    @Test
    @DisplayName("Teacher B cannot create, read, update, or delete sections in Teacher A's course (403)")
    @WithMockUser(username = "teacher_b@example.com", roles = "TEACHER")
    void teacherCannotAccessAnotherTeacherSections() throws Exception {
        SectionCreateReq createReq = SectionCreateReq.builder()
                .title("Malicious Section")
                .build();

        mockMvc.perform(post("/api/v1/admin/courses/" + courseA.getId() + "/sections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/courses/" + courseA.getId() + "/sections"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        SectionUpdateReq updateReq = SectionUpdateReq.builder()
                .title("Updated Section")
                .status(CourseStatus.DRAFT)
                .build();

        mockMvc.perform(put("/api/v1/admin/sections/" + sectionA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(delete("/api/v1/admin/sections/" + sectionA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
    }

    // =========================================================================
    // 3. LESSON ISOLATION
    // =========================================================================

    @Test
    @DisplayName("Teacher B cannot create, read, update, or delete lessons in Teacher A's course (403)")
    @WithMockUser(username = "teacher_b@example.com", roles = "TEACHER")
    void teacherCannotAccessAnotherTeacherLessons() throws Exception {
        LessonCreateReq createReq = LessonCreateReq.builder()
                .title("Malicious Lesson")
                .build();

        mockMvc.perform(post("/api/v1/admin/sections/" + sectionA.getId() + "/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/sections/" + sectionA.getId() + "/lessons"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/lessons/" + lessonA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        LessonUpdateReq updateReq = LessonUpdateReq.builder()
                .title("Updated Lesson")
                .isPreview(false)
                .status(CourseStatus.DRAFT)
                .build();

        mockMvc.perform(put("/api/v1/admin/lessons/" + lessonA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(delete("/api/v1/admin/lessons/" + lessonA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
    }

    // =========================================================================
    // 4. LESSON RESOURCE ISOLATION
    // =========================================================================

    @Test
    @DisplayName("Teacher B cannot create, read, update, or delete resources in Teacher A's lesson (403)")
    @WithMockUser(username = "teacher_b@example.com", roles = "TEACHER")
    void teacherCannotAccessAnotherTeacherLessonResources() throws Exception {
        ResourceCreateReq createReq = ResourceCreateReq.builder()
                .title("Malicious Resource")
                .resourceType(ResourceType.PDF)
                .fileUrl("https://example.com/malicious.pdf")
                .build();

        mockMvc.perform(post("/api/v1/admin/lessons/" + lessonA.getId() + "/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/lessons/" + lessonA.getId() + "/resources"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/resources/" + resourceA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        ResourceUpdateReq updateReq = ResourceUpdateReq.builder()
                .title("Updated Resource")
                .resourceType(ResourceType.PDF)
                .fileUrl("https://example.com/updated.pdf")
                .build();

        mockMvc.perform(put("/api/v1/admin/resources/" + resourceA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(delete("/api/v1/admin/resources/" + resourceA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
    }

    // =========================================================================
    // 5. QUIZ ISOLATION
    // =========================================================================

    @Test
    @DisplayName("Teacher B cannot access or modify Teacher A's quizzes (403)")
    @WithMockUser(username = "teacher_b@example.com", roles = "TEACHER")
    void teacherCannotAccessAnotherTeacherQuizzes() throws Exception {
        QuizCreateReq createReq = new QuizCreateReq();
        createReq.setCourseId(courseA.getId());
        createReq.setTitle("Malicious Quiz");

        mockMvc.perform(post("/api/v1/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/quizzes")
                .param("courseId", String.valueOf(courseA.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/quizzes")
                .param("lessonId", String.valueOf(lessonA.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/quizzes/" + quizA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        QuizUpdateReq updateReq = new QuizUpdateReq();
        updateReq.setTitle("Updated Quiz");
        updateReq.setStatus(QuizStatus.DRAFT);

        mockMvc.perform(put("/api/v1/admin/quizzes/" + quizA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(delete("/api/v1/admin/quizzes/" + quizA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(put("/api/v1/admin/quizzes/" + quizA.getId() + "/publish"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(put("/api/v1/admin/quizzes/" + quizA.getId() + "/hide"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("Teacher A cannot query quizzes for empty course owned by Teacher B (403)")
    @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
    void teacherCannotQueryQuizzesOfAnotherTeacherEmptyCourse() throws Exception {
        mockMvc.perform(get("/api/v1/admin/quizzes")
                .param("courseId", String.valueOf(courseB.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
    }

    // =========================================================================
    // 6. QUESTION & ANSWER ISOLATION
    // =========================================================================

    @Test
    @DisplayName("Teacher B cannot access or modify Teacher A's questions and answers (403)")
    @WithMockUser(username = "teacher_b@example.com", roles = "TEACHER")
    void teacherCannotAccessAnotherTeacherQuestionsAndAnswers() throws Exception {
        QuestionCreateReq qCreateReq = new QuestionCreateReq();
        qCreateReq.setQuestionType(QuestionType.SINGLE_CHOICE);
        qCreateReq.setContent("Malicious Question");

        mockMvc.perform(post("/api/v1/admin/quizzes/" + quizA.getId() + "/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qCreateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/quizzes/" + quizA.getId() + "/questions"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/questions/" + questionA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        QuestionUpdateReq qUpdateReq = new QuestionUpdateReq();
        qUpdateReq.setContent("Updated Question");

        mockMvc.perform(put("/api/v1/admin/questions/" + questionA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qUpdateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(delete("/api/v1/admin/questions/" + questionA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        AnswerCreateReq aCreateReq = new AnswerCreateReq();
        aCreateReq.setContent("Malicious Answer");
        aCreateReq.setIsCorrect(false);

        mockMvc.perform(post("/api/v1/admin/questions/" + questionA.getId() + "/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aCreateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(get("/api/v1/admin/questions/" + questionA.getId() + "/answers"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        AnswerUpdateReq aUpdateReq = new AnswerUpdateReq();
        aUpdateReq.setContent("Updated Answer");
        aUpdateReq.setIsCorrect(true);

        mockMvc.perform(put("/api/v1/admin/answers/" + answerA.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aUpdateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));

        mockMvc.perform(delete("/api/v1/admin/answers/" + answerA.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.DATA_ISOLATION_FORBIDDEN.getCode()));
    }

    // =========================================================================
    // 7. ADMIN PRIVILEGES PRESERVATION
    // =========================================================================

    @Test
    @DisplayName("Admin has full access to Teacher A and Teacher B resources (200 OK)")
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminCanAccessAnyTeacherResource() throws Exception {
        mockMvc.perform(get("/api/v1/admin/courses/" + courseA.getId() + "/sections"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/lessons/" + lessonA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(lessonA.getId()));

        mockMvc.perform(get("/api/v1/admin/resources/" + resourceA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(resourceA.getId()));

        mockMvc.perform(get("/api/v1/admin/quizzes/" + quizA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(quizA.getId()));

        mockMvc.perform(get("/api/v1/admin/questions/" + questionA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(questionA.getId()));

        mockMvc.perform(get("/api/v1/admin/questions/" + questionA.getId() + "/answers"))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // 8. TEACHER ACCESS TO OWN RESOURCES
    // =========================================================================

    @Test
    @DisplayName("Teacher A has full access to own resources (200 OK)")
    @WithMockUser(username = "teacher_a@example.com", roles = "TEACHER")
    void teacherCanAccessOwnResources() throws Exception {
        mockMvc.perform(get("/api/v1/admin/courses/" + courseA.getId() + "/sections"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/lessons/" + lessonA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(lessonA.getId()));

        mockMvc.perform(get("/api/v1/admin/resources/" + resourceA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(resourceA.getId()));

        mockMvc.perform(get("/api/v1/admin/quizzes/" + quizA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(quizA.getId()));

        mockMvc.perform(get("/api/v1/admin/questions/" + questionA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(questionA.getId()));

        mockMvc.perform(get("/api/v1/admin/questions/" + questionA.getId() + "/answers"))
                .andExpect(status().isOk());
    }
}
