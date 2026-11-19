package com.japaneselearning.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.audit.AuditService;
import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.observability.ObservabilityMetrics;
import com.japaneselearning.module_payment.service.PaymentPayloadSanitizer;
import com.japaneselearning.module_auth.dto.RefreshTokenRequest;
import com.japaneselearning.module_auth.entity.RefreshToken;
import com.japaneselearning.module_auth.repository.RefreshTokenRepository;
import com.japaneselearning.module_auth.service.AuthServiceImpl;
import com.japaneselearning.module_auth.util.JwtUtil;
import com.japaneselearning.module_auth.util.TokenHashUtil;
import com.japaneselearning.module_payment.gateway.PaymentInitCommand;
import com.japaneselearning.module_payment.gateway.PaymentInitResult;
import com.japaneselearning.module_payment.gateway.vnpay.VnPayPaymentGateway;
import com.japaneselearning.module_payment.gateway.vnpay.VnPayProperties;
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
import com.japaneselearning.module_quiz.service.QuizLearningServiceImpl;
import com.japaneselearning.module_leaderboard.service.ScoringService;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimezoneClockDeterministicBoundaryTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuizAttemptRepository attemptRepository;

    @Mock
    private QuizAttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private ScoringService scoringService;

    @Mock
    private AuditService auditService;

    @Mock
    private ObservabilityMetrics observabilityMetrics;

    // -------------------------------------------------------------------------
    // 1. VNPay Timezone Correctness Tests (Provider-required Asia/Ho_Chi_Minh)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("VNPay: Formats vnp_CreateDate and vnp_ExpireDate in Asia/Ho_Chi_Minh regardless of UTC clock instant")
    void vnpayTimestamps_AlwaysFormattedInAsiaHoChiMinh() {
        // Fixed instant: 2026-06-15 03:30:00 UTC -> in Vietnam (UTC+7) it is 2026-06-15 10:30:00
        Instant fixedUtc = Instant.parse("2026-06-15T03:30:00Z");
        Clock fixedClock = Clock.fixed(fixedUtc, ZoneOffset.UTC);

        VnPayProperties properties = new VnPayProperties();
        properties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        properties.setTmnCode("TEST_TMN");
        properties.setHashSecret("TEST_SECRET_KEY_12345678901234567890");
        properties.setReturnUrl("https://myweb.com/payment/callback");
        properties.setVersion("2.1.0");
        properties.setCommand("pay");
        properties.setOrderType("other");

        PaymentPayloadSanitizer sanitizer = new PaymentPayloadSanitizer(new ObjectMapper());
        VnPayPaymentGateway gateway = new VnPayPaymentGateway(properties, sanitizer, fixedClock);

        PaymentInitCommand command = PaymentInitCommand.builder()
                .orderCode("ORD-20260615-001")
                .amount(BigDecimal.valueOf(500000))
                .orderInfo("Payment for course JLPT N2")
                .clientIp("192.168.1.10")
                .returnUrl("https://myweb.com/callback")
                .build();

        PaymentInitResult result = gateway.initPayment(command);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentUrl()).isNotBlank();

        // Extract query parameters
        String queryString = result.getPaymentUrl().substring(result.getPaymentUrl().indexOf('?') + 1);
        Map<String, String> queryParams = parseQueryString(queryString);

        // Expected in Vietnam local time (UTC + 7):
        // 03:30 UTC -> 10:30 Vietnam -> "20260615103000"
        // Expire: 15 mins later -> 10:45 Vietnam -> "20260615104500"
        assertThat(queryParams.get("vnp_CreateDate")).isEqualTo("20260615103000");
        assertThat(queryParams.get("vnp_ExpireDate")).isEqualTo("20260615104500");
        assertThat(result.getTransactionCode()).contains("20260615103000");
    }

    @Test
    @DisplayName("VNPay: Cross-midnight rollover in Vietnam time behaves correctly when UTC is still previous day")
    void vnpayTimestamps_CrossMidnightRolloverInVietnam() {
        // UTC is 2026-12-31 17:30:00 -> Vietnam is 2027-01-01 00:30:00 (New Year!)
        Instant fixedUtc = Instant.parse("2026-12-31T17:30:00Z");
        Clock fixedClock = Clock.fixed(fixedUtc, ZoneOffset.UTC);

        VnPayProperties properties = new VnPayProperties();
        properties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        properties.setTmnCode("TEST_TMN");
        properties.setHashSecret("TEST_SECRET_KEY_12345678901234567890");
        properties.setReturnUrl("https://myweb.com/payment/callback");

        PaymentPayloadSanitizer sanitizer = new PaymentPayloadSanitizer(new ObjectMapper());
        VnPayPaymentGateway gateway = new VnPayPaymentGateway(properties, sanitizer, fixedClock);

        PaymentInitCommand command = PaymentInitCommand.builder()
                .orderCode("ORD-NEWYEAR-001")
                .amount(BigDecimal.valueOf(100000))
                .build();

        PaymentInitResult result = gateway.initPayment(command);
        Map<String, String> params = parseQueryString(result.getPaymentUrl().substring(result.getPaymentUrl().indexOf('?') + 1));

        assertThat(params.get("vnp_CreateDate")).isEqualTo("20270101003000");
        assertThat(params.get("vnp_ExpireDate")).isEqualTo("20270101004500");
    }

    // -------------------------------------------------------------------------
    // 2. Quiz Attempt Expiration & Grace Period Boundary Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Quiz Expiration: Submit allowed at exactly 29 seconds past expiresAt (within 30s grace period)")
    void quizExpiration_SubmitWithin30sGracePeriod_Allowed() {
        Instant baseTime = Instant.parse("2026-05-10T10:00:00Z");
        LocalDateTime expiresAt = LocalDateTime.ofInstant(baseTime.plus(15, ChronoUnit.MINUTES), ZoneOffset.UTC);

        // Current clock is at expiresAt + 29 seconds (grace period is 30s)
        Instant submitTime = baseTime.plus(15, ChronoUnit.MINUTES).plusSeconds(29);
        Clock fixedClock = Clock.fixed(submitTime, ZoneOffset.UTC);

        QuizLearningServiceImpl service = new QuizLearningServiceImpl(
                quizRepository, questionRepository, answerRepository,
                attemptRepository, attemptAnswerRepository, null,
                userRepository, null, scoringService, observabilityMetrics, fixedClock
        );

        User student = User.builder().id(1L).email("student@test.com").build();
        Quiz quiz = Quiz.builder().id(100L).status(QuizStatus.PUBLISHED).timeLimitMinutes(15).passingScore(BigDecimal.valueOf(50)).build();
        QuizAttempt attempt = QuizAttempt.builder()
                .id(50L)
                .user(student)
                .quiz(quiz)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.ofInstant(baseTime, ZoneOffset.UTC))
                .expiresAt(expiresAt)
                .build();

        Question q1 = Question.builder().id(10L).quiz(quiz).questionType(QuestionType.SINGLE_CHOICE).points(BigDecimal.TEN).build();
        Answer a1 = Answer.builder().id(100L).question(q1).content("Correct").isCorrect(true).build();

        setupSecurityContext("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findById(50L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(q1));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(10L))).thenReturn(List.of(a1));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(50L);
        req.setAnswers(List.of(QuizSubmitAnswerReq.builder().questionId(10L).answerIds(List.of(100L)).build()));

        assertDoesNotThrow(() -> service.submitAttempt(100L, req));
    }

    @Test
    @DisplayName("Quiz Expiration: Submit rejected at 31 seconds past expiresAt (exceeded 30s grace period)")
    void quizExpiration_SubmitPast30sGracePeriod_ThrowsExpired() {
        Instant baseTime = Instant.parse("2026-05-10T10:00:00Z");
        LocalDateTime expiresAt = LocalDateTime.ofInstant(baseTime.plus(15, ChronoUnit.MINUTES), ZoneOffset.UTC);

        // Current clock is at expiresAt + 31 seconds (grace period 30s expired)
        Instant submitTime = baseTime.plus(15, ChronoUnit.MINUTES).plusSeconds(31);
        Clock fixedClock = Clock.fixed(submitTime, ZoneOffset.UTC);

        QuizLearningServiceImpl service = new QuizLearningServiceImpl(
                quizRepository, questionRepository, answerRepository,
                attemptRepository, attemptAnswerRepository, null,
                userRepository, null, scoringService, observabilityMetrics, fixedClock
        );

        User student = User.builder().id(1L).email("student@test.com").build();
        Quiz quiz = Quiz.builder().id(100L).status(QuizStatus.PUBLISHED).timeLimitMinutes(15).passingScore(BigDecimal.valueOf(50)).build();
        QuizAttempt attempt = QuizAttempt.builder()
                .id(50L)
                .user(student)
                .quiz(quiz)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.ofInstant(baseTime, ZoneOffset.UTC))
                .expiresAt(expiresAt)
                .build();

        setupSecurityContext("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findById(50L)).thenReturn(Optional.of(attempt));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(50L);
        req.setAnswers(List.of(QuizSubmitAnswerReq.builder().questionId(10L).build()));

        assertThatThrownBy(() -> service.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_ATTEMPT_EXPIRED);

        assertThat(attempt.getStatus()).isEqualTo(QuizAttemptStatus.EXPIRED);
        verify(attemptRepository).save(attempt);
    }

    // -------------------------------------------------------------------------
    // 3. Refresh Token Expiration Boundary Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Refresh Token: Valid at 1 second before expiration; Expired at 1 second after expiration")
    void refreshToken_ExpirationBoundary_DeterministicBehavior() {
        Instant expirationInstant = Instant.parse("2026-08-01T12:00:00Z");
        LocalDateTime tokenExpiry = LocalDateTime.ofInstant(expirationInstant, ZoneOffset.UTC);

        // 1. Clock set 1 second before expiry -> Valid
        Clock validClock = Clock.fixed(expirationInstant.minusSeconds(1), ZoneOffset.UTC);
        AuthServiceImpl authServiceValid = new AuthServiceImpl(
                userRepository, null, refreshTokenRepository, null, jwtUtil, observabilityMetrics, null, auditService, validClock
        );

        String rawToken = "test-refresh-token";
        String hash = TokenHashUtil.hashToken(rawToken);
        User user = User.builder().id(10L).email("user@test.com").status(com.japaneselearning.module_user.enums.UserStatus.ACTIVE).roles(Set.of()).build();

        RefreshToken tokenEntity = RefreshToken.builder()
                .id(1L)
                .tokenHash(hash)
                .user(user)
                .expiredAt(tokenExpiry)
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(tokenEntity));
        when(jwtUtil.extractRefreshEmail(rawToken)).thenReturn("user@test.com");
        when(jwtUtil.isRefreshTokenValid(rawToken, "user@test.com")).thenReturn(true);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user)).thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("refresh-token-456");

        assertDoesNotThrow(() -> authServiceValid.refreshToken(new RefreshTokenRequest(rawToken)));

        // 2. Clock set 1 second after expiry -> Throws REFRESH_TOKEN_EXPIRED
        Clock expiredClock = Clock.fixed(expirationInstant.plusSeconds(1), ZoneOffset.UTC);
        AuthServiceImpl authServiceExpired = new AuthServiceImpl(
                userRepository, null, refreshTokenRepository, null, jwtUtil, observabilityMetrics, null, auditService, expiredClock
        );

        // Reset entity revocation flag for next test
        tokenEntity.setRevoked(false);

        assertThatThrownBy(() -> authServiceExpired.refreshToken(new RefreshTokenRequest(rawToken)))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    // -------------------------------------------------------------------------
    // 4. Canonical UTC Date Rollover & Monotonicity
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Canonical UTC: Date and Year roll over deterministically at 23:59:59Z -> 00:00:00Z")
    void dateRollover_UtcMidnight_RollsOverDeterministically() {
        Instant secondBeforeNewYear = Instant.parse("2026-12-31T23:59:59Z");
        Instant newYearMidnight = Instant.parse("2027-01-01T00:00:00Z");

        LocalDate dateBefore = secondBeforeNewYear.atZone(DateTimeConfig.ZONE_UTC).toLocalDate();
        LocalDate dateAfter = newYearMidnight.atZone(DateTimeConfig.ZONE_UTC).toLocalDate();

        assertThat(dateBefore).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(dateAfter).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(dateAfter).isEqualTo(dateBefore.plusDays(1));
    }

    // -------------------------------------------------------------------------
    // 5. Daylight Saving Time Invariance (UTC is unaffected by DST)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Daylight Saving: UTC clock instants remain strictly monotonic and uniform across DST shifts")
    void daylightSaving_UtcClockRemainsInvariant() {
        // US Eastern Daylight Time Spring Forward: 2026-03-08 02:00 -> clocks advance to 03:00
        ZoneId usEastern = ZoneId.of("America/New_York");

        ZonedDateTime beforeDst = ZonedDateTime.of(2026, 3, 8, 1, 30, 0, 0, usEastern);
        ZonedDateTime afterDst = ZonedDateTime.of(2026, 3, 8, 3, 30, 0, 0, usEastern);

        // In local wall-clock time, difference appears to be 2 hours
        // But actual physical duration elapsed is only 1 hour!
        Duration elapsedPhysical = Duration.between(beforeDst.toInstant(), afterDst.toInstant());
        assertThat(elapsedPhysical).isEqualTo(Duration.ofHours(1));

        // When stored as canonical UTC Instant, math is always exact and unaffected by DST
        Instant utc1 = beforeDst.toInstant();
        Instant utc2 = afterDst.toInstant();
        assertThat(ChronoUnit.HOURS.between(utc1, utc2)).isEqualTo(1);

        // Adding 24 hours in UTC is always exactly 24 physical hours
        Instant plus24h = utc1.plus(24, ChronoUnit.HOURS);
        assertThat(Duration.between(utc1, plus24h)).isEqualTo(Duration.ofHours(24));
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private void setupSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    private Map<String, String> parseQueryString(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) {
            return map;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }
}
