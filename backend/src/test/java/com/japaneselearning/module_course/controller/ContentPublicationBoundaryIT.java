package com.japaneselearning.module_course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_learning.repository.LessonProgressRepository;
import com.japaneselearning.module_order.repository.OrderItemRepository;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_quiz.dto.QuizSubmitAnswerReq;
import com.japaneselearning.module_quiz.dto.QuizSubmitReq;
import com.japaneselearning.module_quiz.entity.Answer;
import com.japaneselearning.module_quiz.entity.Question;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.entity.QuizAttempt;
import com.japaneselearning.module_quiz.enums.QuestionType;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ContentPublicationBoundaryIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User teacher;
    private User enrolledStudent;
    private User unenrolledStudent;

    // Published course fixture
    private Course publishedCourse;
    private CourseSection publishedSection1;
    private CourseSection draftSection;
    private CourseSection hiddenSection;
    private CourseSection publishedSection2;

    private Lesson publishedPreviewLesson;
    private Lesson publishedNonPreviewLesson;
    private Lesson draftLessonInSection1;
    private Lesson lessonInDraftSection;
    private Lesson lessonInHiddenSection;
    private Lesson hiddenLessonInSection2;

    // Unpublished courses
    private Course draftCourse;
    private Course hiddenCourse;

    // Quizzes
    private Quiz publishedQuizInPublishedLesson;
    private Quiz draftQuizInPublishedLesson;
    private Quiz quizInDraftLesson;
    private Quiz quizInDraftSection;
    private Quiz quizInDraftCourse;

    private Question question1;
    private Answer correctAnswer1;

    @BeforeEach
    void setUp() {
        // FK-safe teardown
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

        // Seed roles if needed
        Role studentRole = roleRepository.findByName(RoleName.STUDENT).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleName.STUDENT).description("Student").build()));
        Role teacherRole = roleRepository.findByName(RoleName.TEACHER).orElseGet(() ->
                roleRepository.save(Role.builder().name(RoleName.TEACHER).description("Teacher").build()));

        // Seed users
        teacher = userRepository.save(User.builder()
                .email("sensei@example.com")
                .fullName("Yamada Sensei")
                .passwordHash("$2a$10$dummyHashForTestingPurposesOnly12345678901234567890")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(teacherRole))
                .build());

        enrolledStudent = userRepository.save(User.builder()
                .email("enrolled@example.com")
                .fullName("Enrolled Student")
                .passwordHash("$2a$10$dummyHashForTestingPurposesOnly12345678901234567890")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(studentRole))
                .build());

        unenrolledStudent = userRepository.save(User.builder()
                .email("unenrolled@example.com")
                .fullName("Unenrolled Student")
                .passwordHash("$2a$10$dummyHashForTestingPurposesOnly12345678901234567890")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(studentRole))
                .build());

        // 1. Seed Published Course
        publishedCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N3 Master")
                .slug("khoa-hoc-n3-master-publication")
                .shortDescription("Khóa học N3 toàn diện")
                .description("Mô tả chi tiết khóa học N3")
                .level(CourseLevel.N3)
                .courseType(CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(600000))
                .salePrice(BigDecimal.valueOf(450000))
                .status(CourseStatus.PUBLISHED)
                .totalDurationMinutes(180)
                .totalLessons(2)
                .build());

        // 2. Seed Draft Course
        draftCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N2 Đang Soạn Thảo")
                .slug("khoa-hoc-n2-draft-publication")
                .shortDescription("Bản thảo N2")
                .level(CourseLevel.N2)
                .courseType(CourseType.PAID)
                .status(CourseStatus.DRAFT)
                .build());

        // 3. Seed Hidden Course
        hiddenCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N1 Tạm Ẩn")
                .slug("khoa-hoc-n1-hidden-publication")
                .shortDescription("Khóa học N1 ẩn")
                .level(CourseLevel.N1)
                .courseType(CourseType.PAID)
                .status(CourseStatus.HIDDEN)
                .build());

        // Seed Sections for Published Course
        publishedSection1 = sectionRepository.save(CourseSection.builder()
                .course(publishedCourse)
                .title("Chương 1: Ngữ pháp nhập môn (Published)")
                .sortOrder(1)
                .status(CourseStatus.PUBLISHED)
                .build());

        draftSection = sectionRepository.save(CourseSection.builder()
                .course(publishedCourse)
                .title("Chương 2: Đọc hiểu nâng cao (Draft)")
                .sortOrder(2)
                .status(CourseStatus.DRAFT)
                .build());

        hiddenSection = sectionRepository.save(CourseSection.builder()
                .course(publishedCourse)
                .title("Chương 3: Từ vựng chuyên sâu (Hidden)")
                .sortOrder(3)
                .status(CourseStatus.HIDDEN)
                .build());

        publishedSection2 = sectionRepository.save(CourseSection.builder()
                .course(publishedCourse)
                .title("Chương 4: Luyện đề tổng hợp (Published)")
                .sortOrder(4)
                .status(CourseStatus.PUBLISHED)
                .build());

        // Seed Lessons in Published Section 1
        publishedPreviewLesson = lessonRepository.save(Lesson.builder()
                .course(publishedCourse)
                .section(publishedSection1)
                .title("Bài 1.1: Giới thiệu khóa học (Preview)")
                .slug("bai-1-1-gioi-thieu-preview")
                .sortOrder(1)
                .durationMinutes(15)
                .isPreview(true)
                .videoUrl("https://example.com/preview-video.mp4")
                .content("Nội dung học thử công khai")
                .status(CourseStatus.PUBLISHED)
                .build());

        publishedNonPreviewLesson = lessonRepository.save(Lesson.builder()
                .course(publishedCourse)
                .section(publishedSection1)
                .title("Bài 1.2: Ngữ pháp N3 Bài 1 (Locked)")
                .slug("bai-1-2-ngu-phap-n3-bai-1")
                .sortOrder(2)
                .durationMinutes(30)
                .isPreview(false)
                .videoUrl("https://example.com/locked-video.mp4")
                .content("Nội dung bài học khóa cần ghi danh")
                .status(CourseStatus.PUBLISHED)
                .build());

        draftLessonInSection1 = lessonRepository.save(Lesson.builder()
                .course(publishedCourse)
                .section(publishedSection1)
                .title("Bài 1.3: Bài tập bổ trợ (Draft)")
                .slug("bai-1-3-bai-tap-bo-tro-draft")
                .sortOrder(3)
                .durationMinutes(20)
                .isPreview(true) // Even if isPreview is true, draft must NOT be exposed!
                .videoUrl("https://example.com/draft-video.mp4")
                .content("Nội dung bài tập đang soạn thảo")
                .status(CourseStatus.DRAFT)
                .build());

        // Seed Lesson in Draft Section
        lessonInDraftSection = lessonRepository.save(Lesson.builder()
                .course(publishedCourse)
                .section(draftSection)
                .title("Bài 2.1: Đọc hiểu JLPT bài 1")
                .slug("bai-2-1-doc-hieu-jlpt-bai-1")
                .sortOrder(1)
                .durationMinutes(25)
                .isPreview(false)
                .videoUrl("https://example.com/draft-sec-video.mp4")
                .content("Nội dung thuộc chương draft")
                .status(CourseStatus.PUBLISHED) // Lesson says published, but Section is DRAFT!
                .build());

        // Seed Lesson in Hidden Section
        lessonInHiddenSection = lessonRepository.save(Lesson.builder()
                .course(publishedCourse)
                .section(hiddenSection)
                .title("Bài 3.1: Từ vựng chuyên đề")
                .slug("bai-3-1-tu-vung-chuyen-de")
                .sortOrder(1)
                .durationMinutes(20)
                .isPreview(false)
                .videoUrl("https://example.com/hidden-sec-video.mp4")
                .content("Nội dung thuộc chương hidden")
                .status(CourseStatus.PUBLISHED) // Lesson says published, but Section is HIDDEN!
                .build());

        // Seed Hidden Lesson in Published Section 2
        hiddenLessonInSection2 = lessonRepository.save(Lesson.builder()
                .course(publishedCourse)
                .section(publishedSection2)
                .title("Bài 4.1: Đề thi thử số 1 (Hidden)")
                .slug("bai-4-1-de-thi-thu-so-1")
                .sortOrder(1)
                .durationMinutes(40)
                .isPreview(false)
                .videoUrl("https://example.com/hidden-lesson-video.mp4")
                .content("Đề thi thử tạm ẩn")
                .status(CourseStatus.HIDDEN)
                .build());

        // Seed Lesson Resource
        resourceRepository.save(LessonResource.builder()
                .lesson(publishedPreviewLesson)
                .title("Tài liệu bài 1.1 PDF")
                .resourceType(ResourceType.PDF)
                .fileUrl("https://example.com/preview-docs.pdf")
                .fileSize(1024000L)
                .sortOrder(1)
                .build());

        resourceRepository.save(LessonResource.builder()
                .lesson(publishedNonPreviewLesson)
                .title("Tài liệu bài 1.2 PDF")
                .resourceType(ResourceType.PDF)
                .fileUrl("https://example.com/locked-docs.pdf")
                .fileSize(2048000L)
                .sortOrder(1)
                .build());

        resourceRepository.save(LessonResource.builder()
                .lesson(draftLessonInSection1)
                .title("Tài liệu bài draft PDF")
                .resourceType(ResourceType.PDF)
                .fileUrl("https://example.com/draft-docs.pdf")
                .fileSize(512000L)
                .sortOrder(1)
                .build());

        // Enroll student_enrolled in publishedCourse
        enrollmentRepository.save(CourseEnrollment.builder()
                .user(enrolledStudent)
                .course(publishedCourse)
                .status(EnrollmentStatus.ACTIVE)
                .progressPercent(0)
                .build());

        // Seed Quizzes
        publishedQuizInPublishedLesson = quizRepository.save(Quiz.builder()
                .course(publishedCourse)
                .lesson(publishedNonPreviewLesson)
                .title("Quiz N3 Bài 1.2 (Published)")
                .description("Kiểm tra ngữ pháp")
                .timeLimitMinutes(15)
                .passingScore(BigDecimal.valueOf(80))
                .maxAttempts(3)
                .status(QuizStatus.PUBLISHED)
                .build());

        draftQuizInPublishedLesson = quizRepository.save(Quiz.builder()
                .course(publishedCourse)
                .lesson(publishedNonPreviewLesson)
                .title("Quiz N3 Bài 1.2 Nháp (Draft)")
                .status(QuizStatus.DRAFT)
                .build());

        quizInDraftLesson = quizRepository.save(Quiz.builder()
                .course(publishedCourse)
                .lesson(draftLessonInSection1)
                .title("Quiz Gắn Với Bài Học Draft")
                .status(QuizStatus.PUBLISHED)
                .build());

        quizInDraftSection = quizRepository.save(Quiz.builder()
                .course(publishedCourse)
                .lesson(lessonInDraftSection)
                .title("Quiz Gắn Với Section Draft")
                .status(QuizStatus.PUBLISHED)
                .build());

        quizInDraftCourse = quizRepository.save(Quiz.builder()
                .course(draftCourse)
                .title("Quiz Gắn Với Khóa Học Draft")
                .status(QuizStatus.PUBLISHED)
                .build());

        // Questions for published quiz
        question1 = questionRepository.save(Question.builder()
                .quiz(publishedQuizInPublishedLesson)
                .questionType(QuestionType.SINGLE_CHOICE)
                .content("Cấu trúc ~わけがない có ý nghĩa gì?")
                .points(BigDecimal.valueOf(100))
                .sortOrder(1)
                .build());

        correctAnswer1 = answerRepository.save(Answer.builder()
                .question(question1)
                .content("Không có lý nào / Tuyệt đối không")
                .isCorrect(true)
                .sortOrder(1)
                .build());
    }

    // =========================================================================
    // 1. PUBLIC DETAIL BOUNDARY TESTS (GET /api/v1/courses/{slug})
    // =========================================================================

    @Nested
    @DisplayName("1. Public Course Detail Publication Boundaries")
    class PublicDetailBoundaryTests {

        @Test
        @DisplayName("Only published sections and published lessons are exposed in public course detail")
        void publicDetail_ExposesOnlyPublishedSectionsAndLessons() throws Exception {
            mockMvc.perform(get("/api/v1/courses/" + publishedCourse.getSlug())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    // Only Section 1 and Section 4 should be exposed (Section 2 is DRAFT, Section 3 is HIDDEN)
                    .andExpect(jsonPath("$.result.sections", hasSize(2)))
                    .andExpect(jsonPath("$.result.sections[0].id").value(publishedSection1.getId()))
                    .andExpect(jsonPath("$.result.sections[0].title").value("Chương 1: Ngữ pháp nhập môn (Published)"))
                    .andExpect(jsonPath("$.result.sections[1].id").value(publishedSection2.getId()))
                    .andExpect(jsonPath("$.result.sections[1].title").value("Chương 4: Luyện đề tổng hợp (Published)"))
                    // In Section 1: only Lesson 1.1 (published preview) and Lesson 1.2 (published non-preview) appear
                    // Lesson 1.3 (DRAFT) must be completely filtered out!
                    .andExpect(jsonPath("$.result.sections[0].lessons", hasSize(2)))
                    .andExpect(jsonPath("$.result.sections[0].lessons[0].id").value(publishedPreviewLesson.getId()))
                    .andExpect(jsonPath("$.result.sections[0].lessons[0].isPreview").value(true))
                    .andExpect(jsonPath("$.result.sections[0].lessons[0].videoUrl").value("https://example.com/preview-video.mp4"))
                    .andExpect(jsonPath("$.result.sections[0].lessons[0].content").value("Nội dung học thử công khai"))
                    // Lesson 1.2: non-preview, videoUrl and content must be masked (null)
                    .andExpect(jsonPath("$.result.sections[0].lessons[1].id").value(publishedNonPreviewLesson.getId()))
                    .andExpect(jsonPath("$.result.sections[0].lessons[1].isPreview").value(false))
                    .andExpect(jsonPath("$.result.sections[0].lessons[1].videoUrl").doesNotExist())
                    .andExpect(jsonPath("$.result.sections[0].lessons[1].content").doesNotExist())
                    // In Section 4: Lesson 4.1 is HIDDEN, so lessons list must be empty
                    .andExpect(jsonPath("$.result.sections[1].lessons", hasSize(0)));
        }

        @Test
        @DisplayName("Draft course returns 404 COURSE_NOT_FOUND (4001) for public caller")
        void publicDetail_DraftCourse_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/courses/" + draftCourse.getSlug())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4001));
        }

        @Test
        @DisplayName("Hidden course returns 404 COURSE_NOT_FOUND (4001) for public caller")
        void publicDetail_HiddenCourse_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/courses/" + hiddenCourse.getSlug())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4001));
        }
    }

    // =========================================================================
    // 2. LESSON DETAIL BOUNDARY TESTS (GET /api/v1/lessons/{id})
    // =========================================================================

    @Nested
    @DisplayName("2. Lesson Detail Access & Publication Boundaries")
    class LessonDetailBoundaryTests {

        @Test
        @WithMockUser(username = "unenrolled@example.com", roles = "STUDENT")
        @DisplayName("Published preview lesson allows unenrolled student to view content")
        void lessonDetail_PublishedPreview_Unenrolled_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedPreviewLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.id").value(publishedPreviewLesson.getId()))
                    .andExpect(jsonPath("$.result.content").value("Nội dung học thử công khai"))
                    .andExpect(jsonPath("$.result.videoUrl").value("https://example.com/preview-video.mp4"));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Published non-preview lesson allows enrolled student to view content")
        void lessonDetail_PublishedNonPreview_Enrolled_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedNonPreviewLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.id").value(publishedNonPreviewLesson.getId()))
                    .andExpect(jsonPath("$.result.content").value("Nội dung bài học khóa cần ghi danh"))
                    .andExpect(jsonPath("$.result.videoUrl").value("https://example.com/locked-video.mp4"));
        }

        @Test
        @WithMockUser(username = "unenrolled@example.com", roles = "STUDENT")
        @DisplayName("Published non-preview lesson rejects unenrolled student with 403 FORBIDDEN_ACCESS (3003)")
        void lessonDetail_PublishedNonPreview_Unenrolled_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedNonPreviewLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(3003));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Direct access to DRAFT lesson by ID returns 404 LESSON_NOT_FOUND (4005) even for enrolled student")
        void lessonDetail_DraftLesson_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + draftLessonInSection1.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Direct access to HIDDEN lesson by ID returns 404 LESSON_NOT_FOUND (4005) even for enrolled student")
        void lessonDetail_HiddenLesson_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + hiddenLessonInSection2.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Direct access to lesson in DRAFT section returns 404 LESSON_NOT_FOUND (4005)")
        void lessonDetail_LessonInDraftSection_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + lessonInDraftSection.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Direct access to lesson in HIDDEN section returns 404 LESSON_NOT_FOUND (4005)")
        void lessonDetail_LessonInHiddenSection_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + lessonInHiddenSection.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }
    }

    // =========================================================================
    // 3. CURRICULUM BOUNDARY TESTS (GET /api/v1/lessons/{id}/curriculum)
    // =========================================================================

    @Nested
    @DisplayName("3. Learning Curriculum Publication Boundaries")
    class CurriculumBoundaryTests {

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Curriculum navigation excludes draft/hidden sections and lessons")
        void curriculum_ExcludesUnpublishedSectionsAndLessons() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedNonPreviewLesson.getId() + "/curriculum")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    // Only published sections 1 and 4 are returned
                    .andExpect(jsonPath("$.result.sections", hasSize(2)))
                    .andExpect(jsonPath("$.result.sections[0].id").value(publishedSection1.getId()))
                    .andExpect(jsonPath("$.result.sections[1].id").value(publishedSection2.getId()))
                    // Section 1 has only Lesson 1.1 and 1.2 (Lesson 1.3 Draft is excluded)
                    .andExpect(jsonPath("$.result.sections[0].lessons", hasSize(2)))
                    .andExpect(jsonPath("$.result.sections[0].lessons[0].id").value(publishedPreviewLesson.getId()))
                    .andExpect(jsonPath("$.result.sections[0].lessons[1].id").value(publishedNonPreviewLesson.getId()))
                    // Navigation: previous lesson is 1.1, next lesson is null (no more published lessons)
                    .andExpect(jsonPath("$.result.previousLessonId").value(publishedPreviewLesson.getId()))
                    .andExpect(jsonPath("$.result.nextLessonId").doesNotExist());
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Requesting curriculum for DRAFT lesson returns 404 LESSON_NOT_FOUND (4005)")
        void curriculum_DraftLesson_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + draftLessonInSection1.getId() + "/curriculum")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Requesting curriculum for lesson in DRAFT section returns 404 LESSON_NOT_FOUND (4005)")
        void curriculum_LessonInDraftSection_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + lessonInDraftSection.getId() + "/curriculum")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }

        @Test
        @WithMockUser(username = "unenrolled@example.com", roles = "STUDENT")
        @DisplayName("Unenrolled student on preview lesson can view curriculum")
        void curriculum_UnenrolledPreview_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedPreviewLesson.getId() + "/curriculum")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000));
        }

        @Test
        @WithMockUser(username = "unenrolled@example.com", roles = "STUDENT")
        @DisplayName("Unenrolled student on non-preview lesson cannot view curriculum (403 FORBIDDEN_ACCESS)")
        void curriculum_UnenrolledNonPreview_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedNonPreviewLesson.getId() + "/curriculum")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(3003));
        }
    }

    // =========================================================================
    // 4. LESSON RESOURCE BOUNDARY TESTS (GET /api/v1/lessons/{id}/resources)
    // =========================================================================

    @Nested
    @DisplayName("4. Lesson Resources Publication Boundaries")
    class ResourceBoundaryTests {

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Enrolled student can access resources of published lesson")
        void resources_Enrolled_PublishedLesson_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedNonPreviewLesson.getId() + "/resources")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result", hasSize(1)))
                    .andExpect(jsonPath("$.result[0].title").value("Tài liệu bài 1.2 PDF"));
        }

        @Test
        @WithMockUser(username = "unenrolled@example.com", roles = "STUDENT")
        @DisplayName("Unenrolled student can access resources of published preview lesson")
        void resources_Unenrolled_PreviewLesson_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedPreviewLesson.getId() + "/resources")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result", hasSize(1)))
                    .andExpect(jsonPath("$.result[0].title").value("Tài liệu bài 1.1 PDF"));
        }

        @Test
        @WithMockUser(username = "unenrolled@example.com", roles = "STUDENT")
        @DisplayName("Unenrolled student accessing non-preview resources is rejected with 403 Forbidden")
        void resources_Unenrolled_NonPreviewLesson_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedNonPreviewLesson.getId() + "/resources")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(3003));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Accessing resources of DRAFT lesson returns 404 LESSON_NOT_FOUND (4005)")
        void resources_DraftLesson_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + draftLessonInSection1.getId() + "/resources")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Accessing resources of lesson in DRAFT section returns 404 LESSON_NOT_FOUND (4005)")
        void resources_LessonInDraftSection_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + lessonInDraftSection.getId() + "/resources")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }
    }

    // =========================================================================
    // 5. STUDENT QUIZ BOUNDARY TESTS (/api/v1/quizzes/... & /api/v1/lessons/{id}/quizzes)
    // =========================================================================

    @Nested
    @DisplayName("5. Student Quiz Access Publication Boundaries")
    class QuizBoundaryTests {

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Enrolled student can view published quiz in published lesson")
        void quiz_Enrolled_Published_Returns200() throws Exception {
            mockMvc.perform(get("/api/v1/quizzes/" + publishedQuizInPublishedLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.id").value(publishedQuizInPublishedLesson.getId()))
                    .andExpect(jsonPath("$.result.questions", hasSize(1)));
        }

        @Test
        @WithMockUser(username = "unenrolled@example.com", roles = "STUDENT")
        @DisplayName("Unenrolled student accessing quiz gets 403 FORBIDDEN_ACCESS (3003)")
        void quiz_Unenrolled_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/quizzes/" + publishedQuizInPublishedLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(3003));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Accessing DRAFT quiz returns 400 QUIZ_NOT_PUBLISHED (4017)")
        void quiz_DraftQuiz_Returns400() throws Exception {
            mockMvc.perform(get("/api/v1/quizzes/" + draftQuizInPublishedLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4017));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Accessing quiz attached to DRAFT lesson returns 404 QUIZ_NOT_FOUND (4011)")
        void quiz_QuizInDraftLesson_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/quizzes/" + quizInDraftLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4011));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Accessing quiz attached to lesson in DRAFT section returns 404 QUIZ_NOT_FOUND (4011)")
        void quiz_QuizInDraftSection_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/quizzes/" + quizInDraftSection.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4011));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Accessing quiz attached to DRAFT course returns 404 QUIZ_NOT_FOUND (4011)")
        void quiz_QuizInDraftCourse_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/quizzes/" + quizInDraftCourse.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4011));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Starting attempt on quiz in DRAFT lesson returns 404 QUIZ_NOT_FOUND (4011)")
        void quiz_StartAttemptInDraftLesson_Returns404() throws Exception {
            mockMvc.perform(post("/api/v1/quizzes/" + quizInDraftLesson.getId() + "/start")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4011));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("Submitting attempt on quiz in DRAFT lesson returns 404 QUIZ_NOT_FOUND (4011)")
        void quiz_SubmitAttemptInDraftLesson_Returns404() throws Exception {
            QuizSubmitReq submitReq = new QuizSubmitReq();
            submitReq.setAttemptId(999999L);
            QuizSubmitAnswerReq ans = QuizSubmitAnswerReq.builder()
                    .questionId(1L)
                    .answerId(1L)
                    .build();
            submitReq.setAnswers(List.of(ans));

            mockMvc.perform(post("/api/v1/quizzes/" + quizInDraftLesson.getId() + "/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(submitReq)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4011));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("getLessonQuizzes returns only PUBLISHED quizzes for published lesson")
        void lessonQuizzes_ReturnsOnlyPublished() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedNonPreviewLesson.getId() + "/quizzes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    // Only published quiz is returned; draft quiz is excluded
                    .andExpect(jsonPath("$.result", hasSize(1)))
                    .andExpect(jsonPath("$.result[0].id").value(publishedQuizInPublishedLesson.getId()));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("getLessonQuizzes on DRAFT lesson returns 404 LESSON_NOT_FOUND (4005)")
        void lessonQuizzes_DraftLesson_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + draftLessonInSection1.getId() + "/quizzes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }

        @Test
        @WithMockUser(username = "enrolled@example.com", roles = "STUDENT")
        @DisplayName("getLessonQuizzes on lesson in DRAFT section returns 404 LESSON_NOT_FOUND (4005)")
        void lessonQuizzes_LessonInDraftSection_Returns404() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + lessonInDraftSection.getId() + "/quizzes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4005));
        }

        @Test
        @WithMockUser(username = "unenrolled@example.com", roles = "STUDENT")
        @DisplayName("getLessonQuizzes for unenrolled student returns 403 FORBIDDEN_ACCESS (3003)")
        void lessonQuizzes_Unenrolled_Returns403() throws Exception {
            mockMvc.perform(get("/api/v1/lessons/" + publishedNonPreviewLesson.getId() + "/quizzes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(3003));
        }
    }
}
