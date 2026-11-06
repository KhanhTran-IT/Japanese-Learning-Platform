package com.japaneselearning.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.dto.RegisterRequest;
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
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.entity.Order;
import com.japaneselearning.module_order.entity.OrderItem;
import com.japaneselearning.module_order.enums.OrderStatus;
import com.japaneselearning.module_order.repository.OrderRepository;
import com.japaneselearning.module_payment.dto.PaymentCreateReq;
import com.japaneselearning.module_payment.enums.PaymentMethod;
import com.japaneselearning.module_payment.repository.PaymentRepository;
import com.japaneselearning.module_quiz.dto.QuizSubmitAnswerReq;
import com.japaneselearning.module_quiz.dto.QuizSubmitReq;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End API Contract Regression Integration Test Suite.
 *
 * Validates:
 * 1. Unified ApiResponse Envelope & Error Envelope conformance across all critical endpoints.
 * 2. Success (200), Validation Error (400), Unauthenticated (401), Forbidden (403), Not Found (404).
 * 3. Strict Ownership Isolation (Order, Lesson Learning, Payment, Quiz Attempts).
 * 4. Pagination schemas used by Frontend (Spring Data Page & PageResponse).
 * 5. Enum serialization integrity (OrderStatus, CourseStatus, EnrollmentStatus, PaymentMethod, QuizAttemptStatus).
 * 6. Role Matrix authorization across STUDENT, TEACHER, ADMIN roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ApiContractRegressionIT {

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
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuizAttemptAnswerRepository attemptAnswerRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User studentA;
    private User studentB;
    private User teacher;
    private User admin;

    private Course freeCourse;
    private Course paidCourse;

    private Lesson previewLesson;
    private Lesson paidLesson;

    private Order orderA;
    private Quiz quiz;
    private Question question;
    private Answer correctAnswer;
    private Answer wrongAnswer;

    @BeforeEach
    void setUp() {
        // Clear previous test state
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        attemptAnswerRepository.deleteAll();
        attemptRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();

        // Seed roles
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.STUDENT)
                        .description("Student role")
                        .build()));

        Role teacherRole = roleRepository.findByName(RoleName.TEACHER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.TEACHER)
                        .description("Teacher role")
                        .build()));

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.ADMIN)
                        .description("Admin role")
                        .build()));

        // Seed users
        studentA = userRepository.findByEmail("student_a@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_a@example.com")
                        .fullName("Student Alpha")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(studentRole))
                        .build()));

        studentB = userRepository.findByEmail("student_b@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("student_b@example.com")
                        .fullName("Student Beta")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(studentRole))
                        .build()));

        teacher = userRepository.findByEmail("teacher_contract@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("teacher_contract@example.com")
                        .fullName("Teacher Contract")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(teacherRole))
                        .build()));

        admin = userRepository.findByEmail("admin_contract@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("admin_contract@example.com")
                        .fullName("Admin Contract")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(adminRole))
                        .build()));

        // Seed courses
        freeCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N5 Miễn Phí Contract")
                .slug("khoa-hoc-n5-mien-phi-contract")
                .level(CourseLevel.N5)
                .courseType(CourseType.FREE)
                .originalPrice(BigDecimal.ZERO)
                .salePrice(BigDecimal.ZERO)
                .status(CourseStatus.PUBLISHED)
                .totalStudents(0)
                .build());

        paidCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N3 Trả Phí Contract")
                .slug("khoa-hoc-n3-tra-phi-contract")
                .level(CourseLevel.N3)
                .courseType(CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(500000))
                .salePrice(BigDecimal.valueOf(350000))
                .status(CourseStatus.PUBLISHED)
                .totalStudents(0)
                .build());

        // Seed sections
        CourseSection freeSection = sectionRepository.save(CourseSection.builder()
                .course(freeCourse)
                .title("Chương 1: Khởi động")
                .sortOrder(1)
                .build());

        CourseSection paidSection = sectionRepository.save(CourseSection.builder()
                .course(paidCourse)
                .title("Chương 1: Ngữ pháp N3 nâng cao")
                .sortOrder(1)
                .build());

        // Seed lessons
        lessonRepository.save(Lesson.builder()
                .course(freeCourse)
                .section(freeSection)
                .title("Bài 1: Bảng chữ cái")
                .slug("bai-1-bang-chu-cai-contract")
                .sortOrder(1)
                .durationMinutes(15)
                .isPreview(false)
                .videoUrl("https://example.com/free.mp4")
                .content("Nội dung bài học miễn phí")
                .status(CourseStatus.PUBLISHED)
                .build());

        previewLesson = lessonRepository.save(Lesson.builder()
                .course(paidCourse)
                .section(paidSection)
                .title("Bài 1: Giới thiệu khóa N3 (Học thử)")
                .slug("bai-1-gioi-thieu-khoa-n3-contract")
                .sortOrder(1)
                .durationMinutes(20)
                .isPreview(true)
                .videoUrl("https://example.com/preview.mp4")
                .content("Nội dung bài học xem trước")
                .status(CourseStatus.PUBLISHED)
                .build());

        paidLesson = lessonRepository.save(Lesson.builder()
                .course(paidCourse)
                .section(paidSection)
                .title("Bài 2: Cấu trúc ngữ pháp N3 bài 1")
                .slug("bai-2-ngu-phap-n3-contract")
                .sortOrder(2)
                .durationMinutes(45)
                .isPreview(false)
                .videoUrl("https://example.com/paid.mp4")
                .content("Nội dung bài học trả phí yêu cầu ghi danh")
                .status(CourseStatus.PUBLISHED)
                .build());

        // Student A is enrolled in paidCourse; Student B is not enrolled
        enrollmentRepository.save(CourseEnrollment.builder()
                .user(studentA)
                .course(paidCourse)
                .status(EnrollmentStatus.ACTIVE)
                .progressPercent(0)
                .build());

        // Seed order for Student A
        orderA = Order.builder()
                .user(studentA)
                .orderCode("ORD-CONTRACT-A1")
                .totalAmount(BigDecimal.valueOf(350000))
                .finalAmount(BigDecimal.valueOf(350000))
                .discountAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .note("Đơn hàng mẫu Student A")
                .build();

        orderA.addItem(OrderItem.builder()
                .course(paidCourse)
                .courseTitle(paidCourse.getTitle())
                .price(paidCourse.getSalePrice())
                .quantity(1)
                .build());

        orderA = orderRepository.save(orderA);

        // Seed Quiz with Question & Answers
        quiz = quizRepository.save(Quiz.builder()
                .course(paidCourse)
                .lesson(paidLesson)
                .title("Quiz Ngữ Pháp N3 Contract")
                .description("Kiểm tra ngữ pháp N3")
                .timeLimitMinutes(30)
                .passingScore(BigDecimal.valueOf(80))
                .maxAttempts(3)
                .status(QuizStatus.PUBLISHED)
                .build());

        question = questionRepository.save(Question.builder()
                .quiz(quiz)
                .questionType(QuestionType.SINGLE_CHOICE)
                .content("Câu nào sau đây đúng với cấu trúc ~わけがない?")
                .points(BigDecimal.valueOf(100))
                .sortOrder(1)
                .build());

        correctAnswer = answerRepository.save(Answer.builder()
                .question(question)
                .content("Đáp án A: Biểu thị sự phủ định tuyệt đối")
                .isCorrect(true)
                .sortOrder(1)
                .build());

        wrongAnswer = answerRepository.save(Answer.builder()
                .question(question)
                .content("Đáp án B: Biểu thị nguyện vọng cá nhân")
                .isCorrect(false)
                .sortOrder(2)
                .build());
    }

    // =========================================================================
    // 1. STANDARD ENVELOPE CONTRACT TESTS
    // =========================================================================

    @Test
    @DisplayName("Contract 1.1: Standard Success Response Envelope conforms to { code: 1000, message, result }")
    void testStandardSuccessResponseEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/courses/khoa-hoc-n5-mien-phi-contract")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.result").isNotEmpty())
                .andExpect(jsonPath("$.result.slug").value("khoa-hoc-n5-mien-phi-contract"))
                .andExpect(jsonPath("$.result.courseType").value("FREE"));
    }

    @Test
    @DisplayName("Contract 1.2: Standard Validation Error Envelope conforms to { code: 1005, message }")
    void testStandardValidationErrorEnvelope() throws Exception {
        RegisterRequest invalidReq = RegisterRequest.builder()
                .email("invalid-email-format")
                .password("short")
                .confirmPassword("mismatch")
                .fullName("")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("Contract 1.3: Standard Unauthenticated Envelope conforms to { code: 1006, message }")
    void testStandardUnauthenticatedEnvelope() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHENTICATED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHENTICATED.getMessage()));
    }

    @Test
    @DisplayName("Contract 1.4: Standard Forbidden Envelope conforms to { code: 3003, message }")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testStandardForbiddenEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS.getMessage()));
    }

    @Test
    @DisplayName("Contract 1.5: Standard Resource Not Found Envelope conforms to { code: 4001, message }")
    void testStandardNotFoundEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/courses/khoa-hoc-khong-ton-tai-slug-999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.COURSE_NOT_FOUND.getMessage()));
    }

    // =========================================================================
    // 2. AUTHENTICATION & USER PROFILE CONTRACTS
    // =========================================================================

    @Test
    @DisplayName("Contract 2.1: Register new student account succeeds with 200 and ApiResponse envelope")
    void testRegisterContract_Success() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .email("new_student_contract@example.com")
                .password("Password@123")
                .confirmPassword("Password@123")
                .fullName("New Student Contract")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.email").value("new_student_contract@example.com"))
                .andExpect(jsonPath("$.result.fullName").value("New Student Contract"))
                .andExpect(jsonPath("$.result.roles[0]").value("STUDENT"));
    }

    @Test
    @DisplayName("Contract 2.2: Register duplicate email returns 409 Conflict with EMAIL_ALREADY_EXISTS (2001)")
    void testRegisterContract_DuplicateEmail() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .email("student_a@example.com")
                .password("Password@123")
                .confirmPassword("Password@123")
                .fullName("Duplicate Student")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.getCode()));
    }

    @Test
    @DisplayName("Contract 2.3: Login with invalid credentials returns 401 Unauthorized with LOGIN_FAILED (2002)")
    void testLoginContract_InvalidCredentials() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("student_a@example.com")
                .password("WrongPassword@999")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.LOGIN_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.LOGIN_FAILED.getMessage()));
    }

    @Test
    @DisplayName("Contract 2.4: Current user profile returns 200 with required frontend user fields")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testCurrentUserContract_Success() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.email").value("student_a@example.com"))
                .andExpect(jsonPath("$.result.fullName").value("Student Alpha"))
                .andExpect(jsonPath("$.result.roles", hasItem("STUDENT")))
                .andExpect(jsonPath("$.result.status").value("ACTIVE"));
    }

    // =========================================================================
    // 3. COURSE CATALOG & SPRING DATA PAGINATION CONTRACTS
    // =========================================================================

    @Test
    @DisplayName("Contract 3.1: Public course listing returns Spring Data Page with content, number, totalPages, totalElements")
    void testCourseCatalogPaginationContract() throws Exception {
        mockMvc.perform(get("/api/v1/courses")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.result.number").value(0))
                .andExpect(jsonPath("$.result.size").value(5))
                .andExpect(jsonPath("$.result.totalPages").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.result.totalElements").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.content[0].id").isNumber())
                .andExpect(jsonPath("$.result.content[0].title").isString())
                .andExpect(jsonPath("$.result.content[0].slug").isString())
                .andExpect(jsonPath("$.result.content[0].level").isString())
                .andExpect(jsonPath("$.result.content[0].courseType").isString());
    }

    @Test
    @DisplayName("Contract 3.2: Course detail hides non-preview lesson videoUrl from unauthenticated callers")
    void testCourseDetailVisibilityContract() throws Exception {
        mockMvc.perform(get("/api/v1/courses/khoa-hoc-n3-tra-phi-contract")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.slug").value("khoa-hoc-n3-tra-phi-contract"))
                .andExpect(jsonPath("$.result.sections").isArray())
                .andExpect(jsonPath("$.result.sections[0].lessons").isArray())
                // Preview lesson exposes videoUrl
                .andExpect(jsonPath("$.result.sections[0].lessons[0].isPreview").value(true))
                .andExpect(jsonPath("$.result.sections[0].lessons[0].videoUrl").value("https://example.com/preview.mp4"))
                // Non-preview lesson masks videoUrl to null
                .andExpect(jsonPath("$.result.sections[0].lessons[1].isPreview").value(false))
                .andExpect(jsonPath("$.result.sections[0].lessons[1].videoUrl").doesNotExist());
    }

    // =========================================================================
    // 4. COURSE ENROLLMENT CONTRACTS
    // =========================================================================

    @Test
    @DisplayName("Contract 4.1: Direct enrollment in Free course succeeds with ACTIVE status")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testFreeCourseEnrollmentContract() throws Exception {
        mockMvc.perform(post("/api/v1/courses/" + freeCourse.getId() + "/enroll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Ghi danh thành công"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @DisplayName("Contract 4.2: Direct enrollment in Paid course is rejected with 400 COURSE_CANNOT_ENROLL_PAID (4008)")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testPaidCourseDirectEnrollmentRejected() throws Exception {
        mockMvc.perform(post("/api/v1/courses/" + paidCourse.getId() + "/enroll")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.COURSE_CANNOT_ENROLL_PAID.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.COURSE_CANNOT_ENROLL_PAID.getMessage()));
    }

    @Test
    @DisplayName("Contract 4.3: Duplicate enrollment is rejected with 409 Conflict USER_ALREADY_ENROLLED (4009)")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testDuplicateEnrollmentRejected() throws Exception {
        // studentA is already enrolled in paidCourse in setUp()
        // Let's enroll studentA in freeCourse once, then duplicate
        mockMvc.perform(post("/api/v1/courses/" + freeCourse.getId() + "/enroll"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/courses/" + freeCourse.getId() + "/enroll"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_ENROLLED.getCode()));
    }

    // =========================================================================
    // 5. LEARNING & OWNERSHIP/ENROLLMENT ISOLATION CONTRACTS
    // =========================================================================

    @Test
    @DisplayName("Contract 5.1: Preview lesson in Paid course is accessible to unenrolled student")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testLessonLearning_PreviewLesson_AllowedWithoutEnrollment() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/" + previewLesson.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(previewLesson.getId()))
                .andExpect(jsonPath("$.result.isPreview").value(true))
                .andExpect(jsonPath("$.result.content").value("Nội dung bài học xem trước"));
    }

    @Test
    @DisplayName("Contract 5.2: Non-preview lesson in Paid course rejects unenrolled student with 403 FORBIDDEN_ACCESS (3003)")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testLessonLearning_NonPreviewLesson_OwnershipEnrollmentIsolation() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/" + paidLesson.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS.getMessage()));
    }

    @Test
    @DisplayName("Contract 5.3: Non-preview lesson in Paid course allows enrolled student")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testLessonLearning_EnrolledStudent_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/" + paidLesson.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(paidLesson.getId()))
                .andExpect(jsonPath("$.result.content").value("Nội dung bài học trả phí yêu cầu ghi danh"));
    }

    @Test
    @DisplayName("Contract 5.4: Updating progress on non-enrolled lesson is rejected with 403 Forbidden")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testLessonProgressUpdate_OwnershipEnrollmentIsolation() throws Exception {
        ProgressUpdateReq req = ProgressUpdateReq.builder()
                .watchedPercent(80.0)
                .isCompleted(false)
                .build();

        mockMvc.perform(post("/api/v1/lessons/" + paidLesson.getId() + "/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));
    }

    @Test
    @DisplayName("Contract 5.5: Non-existent lesson returns 404 LESSON_NOT_FOUND (4005)")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testLessonNotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/lessons/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.LESSON_NOT_FOUND.getCode()));
    }

    // =========================================================================
    // 6. ORDER CREATION, PAGINATION & OWNERSHIP ISOLATION CONTRACTS
    // =========================================================================

    @Test
    @DisplayName("Contract 6.1: Creating order for Paid course returns 200 with PENDING status and OrderRes fields")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testOrderCreationContract_PaidCourse() throws Exception {
        OrderCreateReq req = OrderCreateReq.builder()
                .courseId(paidCourse.getId())
                .note("Đăng ký học viên B")
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.orderCode", startsWith("ORD-")))
                .andExpect(jsonPath("$.result.status").value("PENDING"))
                .andExpect(jsonPath("$.result.finalAmount").value(350000.0))
                .andExpect(jsonPath("$.result.items", hasSize(1)))
                .andExpect(jsonPath("$.result.items[0].courseId").value(paidCourse.getId()));
    }

    @Test
    @DisplayName("Contract 6.2: Creating order for Free course returns 400 ORDER_COURSE_NOT_PAID (5003)")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testOrderCreationContract_FreeCourse_Rejected() throws Exception {
        OrderCreateReq req = OrderCreateReq.builder()
                .courseId(freeCourse.getId())
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_COURSE_NOT_PAID.getCode()));
    }

    @Test
    @DisplayName("Contract 6.3: Duplicate PENDING order for same course returns 409 ORDER_DUPLICATE_PENDING (5004)")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testOrderCreationContract_DuplicatePending_Rejected() throws Exception {
        Course pendingCourse = courseRepository.save(Course.builder()
                .teacher(teacher)
                .title("Khóa học N2 Chờ Thanh Toán")
                .slug("khoa-hoc-n2-cho-thanh-toan-contract")
                .level(CourseLevel.N2)
                .courseType(CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(600000))
                .salePrice(BigDecimal.valueOf(450000))
                .status(CourseStatus.PUBLISHED)
                .totalStudents(0)
                .build());

        Order pendingOrder = Order.builder()
                .orderCode("ORD-PENDING-DUP")
                .user(studentA)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(450000))
                .finalAmount(BigDecimal.valueOf(450000))
                .discountAmount(BigDecimal.ZERO)
                .build();

        pendingOrder.addItem(OrderItem.builder()
                .course(pendingCourse)
                .courseTitle(pendingCourse.getTitle())
                .price(pendingCourse.getSalePrice())
                .quantity(1)
                .build());

        orderRepository.save(pendingOrder);

        OrderCreateReq req = OrderCreateReq.builder()
                .courseId(pendingCourse.getId())
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_DUPLICATE_PENDING.getCode()));
    }

    @Test
    @DisplayName("Contract 6.4: My orders returns PageResponse with currentPage, pageSize, totalPages, totalElements, data")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testMyOrdersPaginationContract_PageResponse() throws Exception {
        mockMvc.perform(get("/api/v1/orders/me")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.currentPage").value(0))
                .andExpect(jsonPath("$.result.pageSize").value(10))
                .andExpect(jsonPath("$.result.totalPages").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.result.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.result.data").isArray())
                .andExpect(jsonPath("$.result.data[0].orderCode").value("ORD-CONTRACT-A1"))
                .andExpect(jsonPath("$.result.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Contract 6.5: Cross-student order inspection is rejected with 403 ORDER_ACCESS_DENIED (5005) (Ownership Isolation)")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testOrderOwnershipIsolation_CrossStudentForbidden() throws Exception {
        // studentB tries to view studentA's order
        mockMvc.perform(get("/api/v1/orders/" + orderA.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_ACCESS_DENIED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ORDER_ACCESS_DENIED.getMessage()));
    }

    @Test
    @DisplayName("Contract 6.6: Order owner can view their own order details with 200 OK")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testOrderOwnershipIsolation_OwnerAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/orders/" + orderA.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(orderA.getId()))
                .andExpect(jsonPath("$.result.orderCode").value("ORD-CONTRACT-A1"));
    }

    // =========================================================================
    // 7. PAYMENT CREATION & SIMULATION CONTRACTS
    // =========================================================================

    @Test
    @DisplayName("Contract 7.1: Payment creation for another student's order returns 403 ORDER_ACCESS_DENIED (5005)")
    @WithMockUser(username = "student_b@example.com", roles = "STUDENT")
    void testPaymentCreation_OwnershipIsolation() throws Exception {
        PaymentCreateReq req = PaymentCreateReq.builder()
                .orderId(orderA.getId())
                .paymentMethod(PaymentMethod.VNPAY)
                .returnUrl("http://localhost:5173/student/checkout/vnpay-return")
                .build();

        mockMvc.perform(post("/api/v1/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_ACCESS_DENIED.getCode()));
    }

    @Test
    @DisplayName("Contract 7.2: Owner student initiates VNPay payment successfully returning PaymentInitRes")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testPaymentCreation_OwnerStudent_Success() throws Exception {
        PaymentCreateReq req = PaymentCreateReq.builder()
                .orderId(orderA.getId())
                .paymentMethod(PaymentMethod.VNPAY)
                .returnUrl("http://localhost:5173/student/checkout/vnpay-return")
                .build();

        mockMvc.perform(post("/api/v1/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.paymentUrl", containsString("sandbox.vnpayment.vn")))
                .andExpect(jsonPath("$.result.paymentMethod").value("VNPAY"))
                .andExpect(jsonPath("$.result.paymentProvider").value("VNPAY"))
                .andExpect(jsonPath("$.result.amount").value(350000.0));
    }

    @Test
    @DisplayName("Contract 7.3: Payment simulation in dev/test profile transitions order to PAID and returns SUCCESS")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testPaymentSimulation_OwnerStudent_TransitionsOrderToPaid() throws Exception {
        mockMvc.perform(post("/api/v1/payments/simulate/" + orderA.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.status").value("SUCCESS"))
                .andExpect(jsonPath("$.result.paymentMethod").value("SIMULATION"));

        // Verify order status transitioned to PAID
        mockMvc.perform(get("/api/v1/orders/" + orderA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("PAID"));
    }

    // =========================================================================
    // 8. QUIZ LIFECYCLE & OWNERSHIP ISOLATION CONTRACTS
    // =========================================================================

    @Test
    @DisplayName("Contract 8.1: Quiz taking flow enforces attempt ownership isolation and correct score schema")
    void testQuizLifecycleAndOwnershipIsolation() throws Exception {
        // Step 1: Student A starts quiz
        MvcResult startResult = mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/start")
                        .with(user("student_a@example.com").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.quizId").value(quiz.getId()))
                .andExpect(jsonPath("$.result.status").value("IN_PROGRESS"))
                .andReturn();

        Long attemptId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .path("result").path("attemptId").asLong();

        // Step 2: Student B attempts to submit Student A's attempt -> Expect 403 QUIZ_ATTEMPT_FORBIDDEN (4021)
        QuizSubmitReq submitReq = new QuizSubmitReq();
        submitReq.setAttemptId(attemptId);
        submitReq.setAnswers(List.of(
                QuizSubmitAnswerReq.builder()
                        .questionId(question.getId())
                        .answerId(correctAnswer.getId())
                        .build()
        ));

        mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/submit")
                        .with(user("student_b@example.com").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.QUIZ_ATTEMPT_FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.QUIZ_ATTEMPT_FORBIDDEN.getMessage()));

        // Step 3: Switch back to Student A -> Submits successfully
        mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/submit")
                        .with(user("student_a@example.com").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.attemptId").value(attemptId))
                .andExpect(jsonPath("$.result.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.result.score").value(100.0))
                .andExpect(jsonPath("$.result.passed").value(true))
                .andExpect(jsonPath("$.result.correctCount").value(1))
                .andExpect(jsonPath("$.result.wrongCount").value(0));

        // Step 4: Student B attempts to view Student A's attempt result -> Expect 403 QUIZ_ATTEMPT_FORBIDDEN (4021)
        mockMvc.perform(get("/api/v1/quizzes/" + quiz.getId() + "/result/" + attemptId)
                        .with(user("student_b@example.com").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.QUIZ_ATTEMPT_FORBIDDEN.getCode()));
    }

    // =========================================================================
    // 9. ROLE-BASED ACCESS CONTROL (RBAC) MATRIX CONTRACTS
    // =========================================================================

    @Test
    @DisplayName("Contract 9.1: Student accessing Admin course management is rejected with 403 Forbidden")
    @WithMockUser(username = "student_a@example.com", roles = "STUDENT")
    void testRoleMatrix_StudentCallingAdminCourses_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));
    }

    @Test
    @DisplayName("Contract 9.2: Teacher accessing User management is rejected with 403 Forbidden")
    @WithMockUser(username = "teacher_contract@example.com", roles = "TEACHER")
    void testRoleMatrix_TeacherCallingUserManagement_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.getCode()));
    }

    @Test
    @DisplayName("Contract 9.3: Teacher accessing Course management is allowed with 200 OK")
    @WithMockUser(username = "teacher_contract@example.com", roles = "TEACHER")
    void testRoleMatrix_TeacherCallingCourseManagement_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.content").isArray());
    }

    @Test
    @DisplayName("Contract 9.4: Admin accessing User management is allowed with 200 OK")
    @WithMockUser(username = "admin_contract@example.com", roles = "ADMIN")
    void testRoleMatrix_AdminCallingUserManagement_Allowed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.data").isArray());
    }

    @Test
    @DisplayName("Contract 9.5: Unauthenticated access to admin routes is rejected with 401 Unauthorized")
    void testRoleMatrix_UnauthenticatedAccessToAdmin_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHENTICATED.getCode()));
    }
}
