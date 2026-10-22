package com.japaneselearning.module_quiz.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_leaderboard.service.ScoringService;
import com.japaneselearning.module_quiz.dto.QuizResultRes;
import com.japaneselearning.module_quiz.dto.QuizSubmitAnswerReq;
import com.japaneselearning.module_quiz.dto.QuizSubmitReq;
import com.japaneselearning.module_quiz.entity.Answer;
import com.japaneselearning.module_quiz.entity.Question;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.entity.QuizAttempt;
import com.japaneselearning.module_quiz.entity.QuizAttemptAnswer;
import com.japaneselearning.module_quiz.enums.QuestionType;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.AnswerRepository;
import com.japaneselearning.module_quiz.repository.QuestionRepository;
import com.japaneselearning.module_quiz.repository.QuizAttemptAnswerRepository;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
import com.japaneselearning.module_quiz.repository.QuizRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizLearningServiceImplTest {

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
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScoringService scoringService;

    @InjectMocks
    private QuizLearningServiceImpl quizLearningService;

    private User student;
    private Quiz quiz;
    private QuizAttempt attempt;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .email("student@test.com")
                .build();

        Course course = Course.builder()
                .id(10L)
                .status(CourseStatus.PUBLISHED)
                .build();

        quiz = Quiz.builder()
                .id(100L)
                .title("JLPT N5 Comprehensive Quiz")
                .status(QuizStatus.PUBLISHED)
                .course(course)
                .passingScore(BigDecimal.valueOf(2.0))
                .timeLimitMinutes(15)
                .build();

        attempt = QuizAttempt.builder()
                .id(500L)
                .quiz(quiz)
                .user(student)
                .attemptNumber(1)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .status(QuizAttemptStatus.IN_PROGRESS)
                .build();

        // Setup Security Context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        lenient().when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========================================================
    // 1. VALID SUBMISSIONS & SCORING TESTS
    // ========================================================

    @Test
    @DisplayName("Single choice correct: awards full points and marks isCorrect=true")
    void submitAttempt_SingleChoice_Correct() {
        Question q1 = Question.builder().id(11L).quiz(quiz).questionType(QuestionType.SINGLE_CHOICE).points(BigDecimal.valueOf(1.0)).build();
        Answer a1 = Answer.builder().id(101L).question(q1).content("A").isCorrect(true).build();
        Answer a2 = Answer.builder().id(102L).question(q1).content("B").isCorrect(false).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(q1));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(11L))).thenReturn(List.of(a1, a2));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(11L).answerId(101L).build()
        ));

        QuizResultRes res = quizLearningService.submitAttempt(100L, req);

        assertThat(res.getScore()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        assertThat(res.getCorrectCount()).isEqualTo(1);
        assertThat(res.getWrongCount()).isEqualTo(0);
        assertThat(res.getTotalQuestions()).isEqualTo(1);

        ArgumentCaptor<QuizAttemptAnswer> captor = ArgumentCaptor.forClass(QuizAttemptAnswer.class);
        verify(attemptAnswerRepository).save(captor.capture());
        QuizAttemptAnswer savedAnswer = captor.getValue();
        assertThat(savedAnswer.getIsCorrect()).isTrue();
        assertThat(savedAnswer.getPointsEarned()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        assertThat(savedAnswer.getAnswer().getId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("Multiple choice exact match: selects all correct answers and no wrong ones -> full points")
    void submitAttempt_MultipleChoice_ExactMatch_FullCredit() {
        Question qMc = Question.builder().id(12L).quiz(quiz).questionType(QuestionType.MULTIPLE_CHOICE).points(BigDecimal.valueOf(2.0)).build();
        Answer c1 = Answer.builder().id(201L).question(qMc).content("Correct 1").isCorrect(true).build();
        Answer c2 = Answer.builder().id(202L).question(qMc).content("Correct 2").isCorrect(true).build();
        Answer w1 = Answer.builder().id(203L).question(qMc).content("Wrong 1").isCorrect(false).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(qMc));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(12L))).thenReturn(List.of(c1, c2, w1));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(12L).answerIds(List.of(201L, 202L)).build()
        ));

        QuizResultRes res = quizLearningService.submitAttempt(100L, req);

        assertThat(res.getScore()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
        assertThat(res.getCorrectCount()).isEqualTo(1);
        assertThat(res.getWrongCount()).isEqualTo(0);
        assertThat(res.getPassed()).isTrue(); // passing score is 2.0

        verify(scoringService).addXp(eq(1L), eq("QUIZ_PASSED"), eq(200), eq(100L), any());
    }

    @Test
    @DisplayName("Multiple choice partial credit: 1 of 2 correct chosen, 0 wrong -> partial credit")
    void submitAttempt_MultipleChoice_PartialCredit() {
        Question qMc = Question.builder().id(12L).quiz(quiz).questionType(QuestionType.MULTIPLE_CHOICE).points(BigDecimal.valueOf(2.0)).build();
        Answer c1 = Answer.builder().id(201L).question(qMc).content("Correct 1").isCorrect(true).build();
        Answer c2 = Answer.builder().id(202L).question(qMc).content("Correct 2").isCorrect(true).build();
        Answer w1 = Answer.builder().id(203L).question(qMc).content("Wrong 1").isCorrect(false).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(qMc));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(12L))).thenReturn(List.of(c1, c2, w1));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        // Student only selected 1 out of 2 correct answers
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(12L).answerIds(List.of(201L)).build()
        ));

        QuizResultRes res = quizLearningService.submitAttempt(100L, req);

        // 1/2 * 2.0 = 1.0 point
        assertThat(res.getScore()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        assertThat(res.getCorrectCount()).isEqualTo(0); // Not 100% correct, counted as wrongCount
        assertThat(res.getWrongCount()).isEqualTo(1);
        assertThat(res.getPassed()).isFalse(); // 1.0 < 2.0

        ArgumentCaptor<QuizAttemptAnswer> captor = ArgumentCaptor.forClass(QuizAttemptAnswer.class);
        verify(attemptAnswerRepository).save(captor.capture());
        QuizAttemptAnswer savedAnswer = captor.getValue();
        assertThat(savedAnswer.getIsCorrect()).isFalse();
        assertThat(savedAnswer.getPointsEarned()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
    }

    @Test
    @DisplayName("Multiple choice guess penalty: 1 correct + 1 wrong -> 0 points (penalty offsets correct)")
    void submitAttempt_MultipleChoice_PenaltyWithWrongSelection() {
        Question qMc = Question.builder().id(12L).quiz(quiz).questionType(QuestionType.MULTIPLE_CHOICE).points(BigDecimal.valueOf(2.0)).build();
        Answer c1 = Answer.builder().id(201L).question(qMc).content("Correct 1").isCorrect(true).build();
        Answer c2 = Answer.builder().id(202L).question(qMc).content("Correct 2").isCorrect(true).build();
        Answer w1 = Answer.builder().id(203L).question(qMc).content("Wrong 1").isCorrect(false).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(qMc));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(12L))).thenReturn(List.of(c1, c2, w1));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        // Student selected 1 correct and 1 wrong: (1 - 1)/2 = 0
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(12L).answerIds(List.of(201L, 203L)).build()
        ));

        QuizResultRes res = quizLearningService.submitAttempt(100L, req);

        assertThat(res.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(res.getCorrectCount()).isEqualTo(0);
        assertThat(res.getWrongCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple choice legacy client single answerId: fallback handles single answerId cleanly")
    void submitAttempt_MultipleChoice_SingleAnswerIdFallback() {
        Question qMc = Question.builder().id(12L).quiz(quiz).questionType(QuestionType.MULTIPLE_CHOICE).points(BigDecimal.valueOf(2.0)).build();
        Answer c1 = Answer.builder().id(201L).question(qMc).content("Correct 1").isCorrect(true).build();
        Answer c2 = Answer.builder().id(202L).question(qMc).content("Correct 2").isCorrect(true).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(qMc));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(12L))).thenReturn(List.of(c1, c2));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        // Legacy single selection payload
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(12L).answerId(201L).build()
        ));

        QuizResultRes res = quizLearningService.submitAttempt(100L, req);

        // 1 of 2 correct = 1.0 point
        assertThat(res.getScore()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
    }

    @Test
    @DisplayName("Fill blank: normalized match (trim, lowercase, whitespace collapse) scores full credit")
    void submitAttempt_FillBlank_NormalizedMatch() {
        Question qFb = Question.builder().id(13L).quiz(quiz).questionType(QuestionType.FILL_BLANK).points(BigDecimal.valueOf(1.5)).build();
        Answer correctAns = Answer.builder().id(301L).question(qFb).content("Tokyo Tower").isCorrect(true).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(qFb));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(13L))).thenReturn(List.of(correctAns));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        // User text has extra whitespace and upper case
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(13L).userAnswerText("   tokyo    tower  ").build()
        ));

        QuizResultRes res = quizLearningService.submitAttempt(100L, req);

        assertThat(res.getScore()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
        assertThat(res.getCorrectCount()).isEqualTo(1);
        assertThat(res.getWrongCount()).isEqualTo(0);
    }

    // ========================================================
    // 2. UNANSWERED QUESTIONS TESTS
    // ========================================================

    @Test
    @DisplayName("Unanswered questions: question not included in answers list gets 0 points and wrongCount increments")
    void submitAttempt_UnansweredQuestions_HandledProperly() {
        Question q1 = Question.builder().id(11L).quiz(quiz).questionType(QuestionType.SINGLE_CHOICE).points(BigDecimal.valueOf(1.0)).build();
        Question q2 = Question.builder().id(12L).quiz(quiz).questionType(QuestionType.SINGLE_CHOICE).points(BigDecimal.valueOf(1.0)).build();
        Answer a1 = Answer.builder().id(101L).question(q1).content("A").isCorrect(true).build();
        Answer a2 = Answer.builder().id(102L).question(q2).content("B").isCorrect(true).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(q1, q2));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(11L, 12L))).thenReturn(List.of(a1, a2));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        // Student only answers q1, leaving q2 completely omitted
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(11L).answerId(101L).build()
        ));

        QuizResultRes res = quizLearningService.submitAttempt(100L, req);

        assertThat(res.getTotalQuestions()).isEqualTo(2);
        assertThat(res.getScore()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        assertThat(res.getCorrectCount()).isEqualTo(1);
        assertThat(res.getWrongCount()).isEqualTo(1);

        verify(attemptAnswerRepository, times(2)).save(any(QuizAttemptAnswer.class));
    }

    // ========================================================
    // 3. DUPLICATE PAYLOAD MANIPULATION TESTS
    // ========================================================

    @Test
    @DisplayName("Duplicate submission: payload with duplicate questionId throws INVALID_REQUEST")
    void submitAttempt_DuplicateQuestionId_ThrowsInvalidRequest() {
        Question q1 = Question.builder().id(11L).quiz(quiz).questionType(QuestionType.SINGLE_CHOICE).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(q1));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(11L).answerId(101L).build(),
                QuizSubmitAnswerReq.builder().questionId(11L).answerId(102L).build()
        ));

        assertThatThrownBy(() -> quizLearningService.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST)
                .hasMessageContaining("Dữ liệu nộp bài chứa câu hỏi trùng lặp");

        verify(attemptAnswerRepository, never()).save(any());
    }

    // ========================================================
    // 4. INVALID & MALFORMED SUBMISSIONS TESTS
    // ========================================================

    @Test
    @DisplayName("Invalid submission: questionId not belonging to current quiz throws INVALID_REQUEST")
    void submitAttempt_ForeignQuestionId_ThrowsInvalidRequest() {
        Question q1 = Question.builder().id(11L).quiz(quiz).questionType(QuestionType.SINGLE_CHOICE).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(q1));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        // 9999L belongs to another quiz
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(9999L).answerId(101L).build()
        ));

        assertThatThrownBy(() -> quizLearningService.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST)
                .hasMessageContaining("Câu hỏi không thuộc bài kiểm tra này");
    }

    @Test
    @DisplayName("Malformed submission: null questionId throws INVALID_REQUEST")
    void submitAttempt_NullQuestionId_ThrowsInvalidRequest() {
        Question q1 = Question.builder().id(11L).quiz(quiz).questionType(QuestionType.SINGLE_CHOICE).build();

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(List.of(q1));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(List.of(
                QuizSubmitAnswerReq.builder().questionId(null).answerId(101L).build()
        ));

        assertThatThrownBy(() -> quizLearningService.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST)
                .hasMessageContaining("questionId không được để trống");
    }

    @Test
    @DisplayName("Malformed submission: empty answers list throws INVALID_REQUEST")
    void submitAttempt_EmptyAnswers_ThrowsInvalidRequest() {
        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(new ArrayList<>());

        assertThatThrownBy(() -> quizLearningService.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST)
                .hasMessageContaining("Danh sách câu trả lời không được rỗng");
    }

    @Test
    @DisplayName("Invalid ownership: submitting other user's attempt throws QUIZ_ATTEMPT_FORBIDDEN")
    void submitAttempt_ForbiddenUser_ThrowsException() {
        User otherUser = User.builder().id(999L).email("other@test.com").build();
        attempt.setUser(otherUser);

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(List.of(QuizSubmitAnswerReq.builder().questionId(11L).build()));

        assertThatThrownBy(() -> quizLearningService.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_ATTEMPT_FORBIDDEN);
    }

    @Test
    @DisplayName("Invalid quiz match: attempt quizId mismatch throws INVALID_REQUEST")
    void submitAttempt_QuizMismatch_ThrowsException() {
        Quiz differentQuiz = Quiz.builder().id(999L).status(QuizStatus.PUBLISHED).build();
        attempt.setQuiz(differentQuiz);

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(List.of(QuizSubmitAnswerReq.builder().questionId(11L).build()));

        assertThatThrownBy(() -> quizLearningService.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("Already submitted: attempt not in progress throws QUIZ_ATTEMPT_ALREADY_SUBMITTED")
    void submitAttempt_AlreadySubmitted_ThrowsException() {
        attempt.setStatus(QuizAttemptStatus.SUBMITTED);

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(List.of(QuizSubmitAnswerReq.builder().questionId(11L).build()));

        assertThatThrownBy(() -> quizLearningService.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_ATTEMPT_ALREADY_SUBMITTED);
    }

    @Test
    @DisplayName("Expired attempt: past deadline throws QUIZ_ATTEMPT_EXPIRED and sets status to EXPIRED")
    void submitAttempt_Expired_ThrowsException() {
        // Started 20 minutes ago, limit is 15 minutes + 30s grace period
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(20));

        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        req.setAnswers(List.of(QuizSubmitAnswerReq.builder().questionId(11L).build()));

        assertThatThrownBy(() -> quizLearningService.submitAttempt(100L, req))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_ATTEMPT_EXPIRED);

        assertThat(attempt.getStatus()).isEqualTo(QuizAttemptStatus.EXPIRED);
        verify(attemptRepository).save(attempt);
    }
}
