package com.japaneselearning.common.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_admin.service.AdminUserService;
import com.japaneselearning.module_auth.dto.LoginRequest;
import com.japaneselearning.module_auth.service.AuthService;
import com.japaneselearning.module_course.dto.CourseCreateReq;
import com.japaneselearning.module_course.dto.CourseRes;
import com.japaneselearning.module_course.dto.LessonCreateReq;
import com.japaneselearning.module_course.dto.LessonRes;
import com.japaneselearning.module_course.service.CourseAdminService;
import com.japaneselearning.module_course.service.LessonAdminService;
import com.japaneselearning.module_flashcard.dto.ReviewFlashcardReq;
import com.japaneselearning.module_flashcard.service.FlashcardService;
import com.japaneselearning.module_order.dto.OrderCreateReq;
import com.japaneselearning.module_order.service.OrderService;
import com.japaneselearning.module_payment.service.PaymentService;
import com.japaneselearning.module_quiz.dto.QuizSubmitAnswerReq;
import com.japaneselearning.module_quiz.dto.QuizSubmitReq;
import com.japaneselearning.module_quiz.service.QuizLearningService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PayloadBoundaryIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseAdminService courseAdminService;

    @MockBean
    private LessonAdminService lessonAdminService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private FlashcardService flashcardService;

    @MockBean
    private QuizLearningService quizLearningService;

    @MockBean
    private AuthService authService;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private PaymentService paymentService;

    // ========================================================
    // 1. Course & Lesson Payload Boundary Tests
    // ========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Course create: description > 50,000 chars is rejected with 400 VALIDATION_ERROR")
    void courseCreate_DescriptionExceedsLimit_ReturnsBadRequest() throws Exception {
        String oversizedDescription = "A".repeat(50001);
        CourseCreateReq req = CourseCreateReq.builder()
                .title("Japanese Course")
                .description(oversizedDescription)
                .level(com.japaneselearning.module_course.enums.CourseLevel.N5)
                .courseType(com.japaneselearning.module_course.enums.CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(100000))
                .build();

        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Course create: shortDescription > 1,000 chars is rejected with 400 VALIDATION_ERROR")
    void courseCreate_ShortDescriptionExceedsLimit_ReturnsBadRequest() throws Exception {
        String oversizedShortDesc = "B".repeat(1001);
        CourseCreateReq req = CourseCreateReq.builder()
                .title("Japanese Course")
                .shortDescription(oversizedShortDesc)
                .level(com.japaneselearning.module_course.enums.CourseLevel.N5)
                .courseType(com.japaneselearning.module_course.enums.CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(100000))
                .build();

        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Course create: legitimate Japanese content with Kanji/Kana passes validation without issue")
    void courseCreate_ValidJapaneseContent_PassesValidation() throws Exception {
        CourseCreateReq req = CourseCreateReq.builder()
                .title("日本語能力試験 N5 完全マスターコース")
                .shortDescription("初心者向けの日本語学習コースです。ひらがな・カタカナから日常会話まで学習します。")
                .description("<h2>コース概要</h2><p>日本語の基礎文法（〜です、〜ます、て形、た形）を徹底的に学びます。漢字約100字、語彙約800語をカバー。</p>")
                .level(com.japaneselearning.module_course.enums.CourseLevel.N5)
                .courseType(com.japaneselearning.module_course.enums.CourseType.PAID)
                .originalPrice(BigDecimal.valueOf(299000))
                .build();

        when(courseAdminService.createCourse(any())).thenReturn(CourseRes.builder().id(1L).title(req.getTitle()).build());

        mockMvc.perform(post("/api/v1/admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Lesson create: content > 100,000 chars is rejected with 400 VALIDATION_ERROR")
    void lessonCreate_ContentExceedsLimit_ReturnsBadRequest() throws Exception {
        String oversizedContent = "X".repeat(100001);
        LessonCreateReq req = LessonCreateReq.builder()
                .title("Lesson Title")
                .content(oversizedContent)
                .build();

        mockMvc.perform(post("/api/v1/admin/sections/1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Lesson create: valid rich Japanese lesson content passes validation")
    void lessonCreate_ValidJapaneseRichContent_PassesValidation() throws Exception {
        LessonCreateReq req = LessonCreateReq.builder()
                .title("第1課：はじめまして")
                .content("<h1>第1課：自己紹介</h1><p>私は学生です。ベトナムから来ました。どうぞよろしくお願いします！</p>")
                .build();

        when(lessonAdminService.createLesson(eq(1L), any())).thenReturn(LessonRes.builder().id(10L).title(req.getTitle()).build());

        mockMvc.perform(post("/api/v1/admin/sections/1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(10L));
    }

    // ========================================================
    // 2. Order Payload Boundary Tests
    // ========================================================

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Order create: note > 1,000 chars is rejected with 400 VALIDATION_ERROR")
    void orderCreate_NoteExceedsLimit_ReturnsBadRequest() throws Exception {
        String oversizedNote = "N".repeat(1001);
        OrderCreateReq req = OrderCreateReq.builder()
                .courseId(1L)
                .note(oversizedNote)
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Order create: Idempotency-Key header > 255 chars is rejected with 400 INVALID_REQUEST")
    void orderCreate_OversizedIdempotencyHeader_ReturnsBadRequest() throws Exception {
        String oversizedKey = "K".repeat(256);
        OrderCreateReq req = OrderCreateReq.builder()
                .courseId(1L)
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", oversizedKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()));
    }

    // ========================================================
    // 3. Flashcard Review Boundary Tests
    // ========================================================

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Flashcard review: invalid difficulty string is rejected with 400 VALIDATION_ERROR")
    void flashcardReview_InvalidDifficulty_ReturnsBadRequest() throws Exception {
        ReviewFlashcardReq req = ReviewFlashcardReq.builder()
                .flashcardId(1L)
                .difficulty("SUPER_HARD")
                .idempotencyKey("idem-key-1")
                .timezone("Asia/Tokyo")
                .build();

        mockMvc.perform(post("/api/v1/flashcards/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Flashcard review: oversized idempotencyKey > 255 chars is rejected with 400 VALIDATION_ERROR")
    void flashcardReview_OversizedIdempotencyKey_ReturnsBadRequest() throws Exception {
        ReviewFlashcardReq req = ReviewFlashcardReq.builder()
                .flashcardId(1L)
                .difficulty("EASY")
                .idempotencyKey("I".repeat(256))
                .timezone("Asia/Tokyo")
                .build();

        mockMvc.perform(post("/api/v1/flashcards/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Flashcard review: valid request with EASY difficulty passes validation")
    void flashcardReview_ValidJapaneseStudyReview_PassesValidation() throws Exception {
        ReviewFlashcardReq req = ReviewFlashcardReq.builder()
                .flashcardId(1L)
                .difficulty("EASY")
                .idempotencyKey("idem-key-valid")
                .timezone("Asia/Tokyo")
                .build();

        mockMvc.perform(post("/api/v1/flashcards/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ========================================================
    // 4. Quiz Submission Boundary Tests
    // ========================================================

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Quiz submit: answers list > 200 items is rejected with 400 VALIDATION_ERROR")
    void quizSubmit_AnswersExceedConstraint_ReturnsBadRequest() throws Exception {
        List<QuizSubmitAnswerReq> answers = new ArrayList<>();
        for (long i = 1; i <= 201; i++) {
            answers.add(QuizSubmitAnswerReq.builder().questionId(i).answerId(i).build());
        }

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(1L);
        req.setAnswers(answers);

        mockMvc.perform(post("/api/v1/quizzes/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Quiz submit: userAnswerText > 1,000 chars is rejected with 400 VALIDATION_ERROR")
    void quizSubmit_UserAnswerTextExceedsLimit_ReturnsBadRequest() throws Exception {
        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(1L);
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder()
                        .questionId(1L)
                        .userAnswerText("T".repeat(1001))
                        .build()
        ));

        mockMvc.perform(post("/api/v1/quizzes/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    // ========================================================
    // 5. Auth Boundary & Format Abuse Tests
    // ========================================================

    @Test
    @DisplayName("Auth login: email > 150 chars is rejected with 400 VALIDATION_ERROR")
    void login_OversizedEmail_ReturnsBadRequest() throws Exception {
        String longEmail = "a".repeat(145) + "@ex.com";
        LoginRequest req = new LoginRequest(longEmail, "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @DisplayName("Malformed JSON payload returns standardized 400 INVALID_REQUEST")
    void malformedJson_ReturnsStandardizedError() throws Exception {
        String malformedJson = "{ \"email\": \"student@example.com\", \"password\": ";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value("Dữ liệu yêu cầu không hợp lệ hoặc sai định dạng JSON"));
    }

    // ========================================================
    // 6. Admin & Payment Abuse Boundary Tests
    // ========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin users listing: keyword > 100 chars is rejected with 400 VALIDATION_ERROR")
    void adminUsers_KeywordExceedsLimit_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("keyword", "K".repeat(101))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.getCode()));
    }

    @Test
    @DisplayName("Payment webhook: rawBody > 64KB is rejected with 400 INVALID_REQUEST")
    void paymentWebhook_OversizedRawBody_ReturnsBadRequest() throws Exception {
        String oversizedBody = "X".repeat(65537);

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oversizedBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()));
    }
}
