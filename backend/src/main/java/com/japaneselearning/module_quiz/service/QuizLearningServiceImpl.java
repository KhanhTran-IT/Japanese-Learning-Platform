package com.japaneselearning.module_quiz.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_quiz.dto.*;
import com.japaneselearning.module_quiz.entity.*;
import com.japaneselearning.module_quiz.enums.QuestionType;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.*;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizLearningServiceImpl implements QuizLearningService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizAttemptAnswerRepository attemptAnswerRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    // ==========================================
    // GET QUIZ FOR STUDENT
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public QuizLearningRes getQuizForStudent(Long quizId) {
        Quiz quiz = getPublishedQuiz(quizId);
        validateQuizAccess(quiz);

        List<Question> questions = questionRepository.findByQuizIdOrderBySortOrderAsc(quizId);

        List<QuestionLearningRes> questionResList = questions.stream().map(q -> {
            List<AnswerLearningRes> answerResList = answerRepository
                    .findByQuestionIdOrderBySortOrderAsc(q.getId())
                    .stream()
                    .map(a -> AnswerLearningRes.builder()
                            .id(a.getId())
                            .content(a.getContent())
                            .sortOrder(a.getSortOrder())
                            .build())
                    .collect(Collectors.toList());

            return QuestionLearningRes.builder()
                    .id(q.getId())
                    .questionType(q.getQuestionType() != null ? q.getQuestionType().name() : null)
                    .content(q.getContent())
                    .audioUrl(q.getAudioUrl())
                    .imageUrl(q.getImageUrl())
                    .points(q.getPoints())
                    .sortOrder(q.getSortOrder())
                    .answers(answerResList)
                    .build();
        }).collect(Collectors.toList());

        return QuizLearningRes.builder()
                .id(quiz.getId())
                .courseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null)
                .lessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null)
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .passingScore(quiz.getPassingScore())
                .maxAttempts(quiz.getMaxAttempts())
                .questions(questionResList)
                .build();
    }

    // ==========================================
    // START ATTEMPT
    // ==========================================

    @Override
    @Transactional
    public QuizAttemptStartRes startAttempt(Long quizId) {
        Quiz quiz = getPublishedQuiz(quizId);
        validateQuizAccess(quiz);
        User user = getCurrentUser();

        // Check maxAttempts
        if (quiz.getMaxAttempts() != null && quiz.getMaxAttempts() > 0) {
            long attemptCount = attemptRepository.countByUserIdAndQuizId(user.getId(), quizId);
            if (attemptCount >= quiz.getMaxAttempts()) {
                throw new AppException(ErrorCode.QUIZ_MAX_ATTEMPTS_REACHED);
            }
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .startedAt(LocalDateTime.now())
                .status(QuizAttemptStatus.IN_PROGRESS)
                .build();

        QuizAttempt saved = attemptRepository.save(attempt);

        long remainingAttempts = -1; // unlimited
        if (quiz.getMaxAttempts() != null && quiz.getMaxAttempts() > 0) {
            long usedAttempts = attemptRepository.countByUserIdAndQuizId(user.getId(), quizId);
            remainingAttempts = quiz.getMaxAttempts() - usedAttempts;
        }

        return QuizAttemptStartRes.builder()
                .attemptId(saved.getId())
                .quizId(quizId)
                .startedAt(saved.getStartedAt())
                .status(saved.getStatus().name())
                .maxAttempts(quiz.getMaxAttempts())
                .remainingAttempts(remainingAttempts < 0 ? null : remainingAttempts)
                .build();
    }

    // ==========================================
    // SUBMIT ATTEMPT
    // ==========================================

    @Override
    @Transactional
    public QuizResultRes submitAttempt(Long quizId, QuizSubmitReq req) {
        Quiz quiz = getPublishedQuiz(quizId);
        User user = getCurrentUser();

        QuizAttempt attempt = attemptRepository.findById(req.getAttemptId())
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_ATTEMPT_NOT_FOUND));

        // Ownership check
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.QUIZ_ATTEMPT_FORBIDDEN);
        }

        // Quiz match check
        if (!attempt.getQuiz().getId().equals(quizId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Already submitted check
        if (attempt.getStatus() != QuizAttemptStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.QUIZ_ATTEMPT_ALREADY_SUBMITTED);
        }

        // Fetch all questions and build answer lookup
        List<Question> questions = questionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // Pre-fetch all answers for this quiz's questions for scoring
        Map<Long, List<Answer>> answersByQuestion = questions.stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        q -> answerRepository.findByQuestionIdOrderBySortOrderAsc(q.getId())
                ));

        // Build a map from submitted answers: questionId -> QuizSubmitAnswerReq
        Map<Long, QuizSubmitAnswerReq> submittedMap = req.getAnswers().stream()
                .collect(Collectors.toMap(QuizSubmitAnswerReq::getQuestionId, a -> a, (a, b) -> b));

        int totalQuestions = questions.size();
        int correctCount = 0;
        int wrongCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;

        // Process each question
        for (Question question : questions) {
            QuizSubmitAnswerReq submitted = submittedMap.get(question.getId());
            List<Answer> questionAnswers = answersByQuestion.getOrDefault(question.getId(), List.of());

            boolean isCorrect = false;
            BigDecimal pointsEarned = BigDecimal.ZERO;
            Answer selectedAnswer = null;
            String userAnswerText = null;

            if (submitted != null) {
                QuestionType type = question.getQuestionType();

                if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.TRUE_FALSE) {
                    // Score by checking if selected answerId is the correct one
                    if (submitted.getAnswerId() != null) {
                        selectedAnswer = questionAnswers.stream()
                                .filter(a -> a.getId().equals(submitted.getAnswerId()))
                                .findFirst().orElse(null);

                        if (selectedAnswer != null && Boolean.TRUE.equals(selectedAnswer.getIsCorrect())) {
                            isCorrect = true;
                            pointsEarned = question.getPoints() != null ? question.getPoints() : BigDecimal.ONE;
                        }
                    }
                } else if (type == QuestionType.FILL_BLANK) {
                    // TODO: FILL_BLANK scoring — currently stores text but scores 0.
                    // Full support requires a correct_text field in Question entity.
                    userAnswerText = submitted.getUserAnswerText();
                } else {
                    // MULTIPLE_CHOICE, MATCHING, LISTENING, REORDER — not fully scored yet.
                    // TODO: Implement advanced scoring for these types.
                    userAnswerText = submitted.getUserAnswerText();
                    if (submitted.getAnswerId() != null) {
                        selectedAnswer = questionAnswers.stream()
                                .filter(a -> a.getId().equals(submitted.getAnswerId()))
                                .findFirst().orElse(null);
                    }
                }
            }

            if (isCorrect) {
                correctCount++;
            } else {
                wrongCount++;
            }
            totalScore = totalScore.add(pointsEarned);

            // Save QuizAttemptAnswer
            QuizAttemptAnswer attemptAnswer = QuizAttemptAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .answer(selectedAnswer)
                    .userAnswerText(userAnswerText)
                    .isCorrect(isCorrect)
                    .pointsEarned(pointsEarned)
                    .build();
            attemptAnswerRepository.save(attemptAnswer);
        }

        // Update attempt
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setScore(totalScore);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setCorrectCount(correctCount);
        attempt.setWrongCount(wrongCount);
        attempt.setStatus(QuizAttemptStatus.SUBMITTED);

        // Determine passed
        BigDecimal passingScore = quiz.getPassingScore() != null ? quiz.getPassingScore() : BigDecimal.ZERO;
        attempt.setPassed(totalScore.compareTo(passingScore) >= 0);

        attemptRepository.save(attempt);

        return buildResultRes(attempt, quiz);
    }

    // ==========================================
    // GET ATTEMPT RESULT
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public QuizResultRes getAttemptResult(Long quizId, Long attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_ATTEMPT_NOT_FOUND));

        if (!attempt.getQuiz().getId().equals(quizId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Ownership or admin check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();
        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdminOrSuperAdmin && !attempt.getUser().getEmail().equals(currentEmail)) {
            throw new AppException(ErrorCode.QUIZ_ATTEMPT_FORBIDDEN);
        }

        Quiz quiz = attempt.getQuiz();
        return buildResultRes(attempt, quiz);
    }

    // ==========================================
    // MY ATTEMPTS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttemptSummaryRes> getMyAttempts() {
        User user = getCurrentUser();
        List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByStartedAtDesc(user.getId());

        return attempts.stream().map(a -> {
            Quiz quiz = a.getQuiz();
            return QuizAttemptSummaryRes.builder()
                    .attemptId(a.getId())
                    .quizId(quiz.getId())
                    .quizTitle(quiz.getTitle())
                    .score(a.getScore())
                    .passingScore(quiz.getPassingScore())
                    .totalQuestions(a.getTotalQuestions())
                    .correctCount(a.getCorrectCount())
                    .passed(a.getPassed())
                    .status(a.getStatus() != null ? a.getStatus().name() : null)
                    .startedAt(a.getStartedAt())
                    .submittedAt(a.getSubmittedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    // ==========================================
    // DISCOVERY
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<QuizDiscoveryRes> getLessonQuizzes(Long lessonId) {
        User user = getCurrentUser();
        List<Quiz> quizzes = quizRepository.findByLessonIdAndStatus(lessonId, QuizStatus.PUBLISHED);

        return quizzes.stream()
                .filter(quiz -> {
                    try {
                        validateQuizAccess(quiz);
                        return true;
                    } catch (AppException e) {
                        return false;
                    }
                })
                .map(quiz -> {
                    int questionCount = questionRepository.countByQuizId(quiz.getId());
                    QuizAttempt latestAttempt = attemptRepository
                            .findFirstByUserIdAndQuizIdOrderByStartedAtDesc(user.getId(), quiz.getId())
                            .orElse(null);

                    long remainingAttempts = -1; // unlimited
                    if (quiz.getMaxAttempts() != null && quiz.getMaxAttempts() > 0) {
                        long usedAttempts = attemptRepository.countByUserIdAndQuizId(user.getId(), quiz.getId());
                        remainingAttempts = quiz.getMaxAttempts() - usedAttempts;
                        if (remainingAttempts < 0) remainingAttempts = 0;
                    }

                    return QuizDiscoveryRes.builder()
                            .id(quiz.getId())
                            .courseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null)
                            .lessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null)
                            .title(quiz.getTitle())
                            .description(quiz.getDescription())
                            .timeLimitMinutes(quiz.getTimeLimitMinutes())
                            .passingScore(quiz.getPassingScore())
                            .maxAttempts(quiz.getMaxAttempts())
                            .questionCount(questionCount)
                            .latestAttemptId(latestAttempt != null ? latestAttempt.getId() : null)
                            .latestAttemptStatus(latestAttempt != null && latestAttempt.getStatus() != null ? latestAttempt.getStatus().name() : null)
                            .latestScore(latestAttempt != null ? latestAttempt.getScore() : null)
                            .latestPassed(latestAttempt != null ? latestAttempt.getPassed() : null)
                            .remainingAttempts(remainingAttempts < 0 ? null : remainingAttempts)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Quiz getPublishedQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));
        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new AppException(ErrorCode.QUIZ_NOT_PUBLISHED);
        }
        return quiz;
    }

    /**
     * Validate enrollment access for quiz.
     * Reuses the same logic as LearningServiceImpl:
     * - If quiz is linked to a course, student must be enrolled.
     * - If quiz is linked to a lesson, check the lesson's course enrollment.
     */
    private void validateQuizAccess(Quiz quiz) {
        User user = getCurrentUser();
        Course course = quiz.getCourse();

        if (course == null && quiz.getLesson() != null) {
            course = quiz.getLesson().getCourse();
        }

        if (course == null) return; // no course linkage, allow access

        // Course must be published
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.QUIZ_NOT_FOUND);
        }

        // Student must be enrolled
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId());
        if (!isEnrolled) {
            throw new AppException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private QuizResultRes buildResultRes(QuizAttempt attempt, Quiz quiz) {
        List<QuizAttemptAnswer> attemptAnswers = attemptAnswerRepository.findByAttemptId(attempt.getId());

        // Build correct answer lookup per question
        Map<Long, Answer> correctAnswerMap = attemptAnswers.stream()
                .map(aa -> aa.getQuestion().getId())
                .distinct()
                .collect(Collectors.toMap(
                        qId -> qId,
                        qId -> answerRepository.findByQuestionIdOrderBySortOrderAsc(qId).stream()
                                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                                .findFirst()
                                .orElse(null)
                ));

        List<QuizResultAnswerRes> answerResList = attemptAnswers.stream().map(aa -> {
            Question q = aa.getQuestion();
            Answer correctAnswer = correctAnswerMap.get(q.getId());

            return QuizResultAnswerRes.builder()
                    .questionId(q.getId())
                    .questionContent(q.getContent())
                    .questionType(q.getQuestionType() != null ? q.getQuestionType().name() : null)
                    .selectedAnswerId(aa.getAnswer() != null ? aa.getAnswer().getId() : null)
                    .selectedAnswerContent(aa.getAnswer() != null ? aa.getAnswer().getContent() : null)
                    .correctAnswerId(correctAnswer != null ? correctAnswer.getId() : null)
                    .correctAnswerContent(correctAnswer != null ? correctAnswer.getContent() : null)
                    .userAnswerText(aa.getUserAnswerText())
                    .isCorrect(aa.getIsCorrect())
                    .pointsEarned(aa.getPointsEarned())
                    .explanation(q.getExplanation())
                    .build();
        }).collect(Collectors.toList());

        return QuizResultRes.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .score(attempt.getScore())
                .passingScore(quiz.getPassingScore())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectCount())
                .wrongCount(attempt.getWrongCount())
                .passed(attempt.getPassed())
                .status(attempt.getStatus() != null ? attempt.getStatus().name() : null)
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .answers(answerResList)
                .build();
    }
}
