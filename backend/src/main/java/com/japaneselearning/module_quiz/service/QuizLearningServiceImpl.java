package com.japaneselearning.module_quiz.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.observability.ObservabilityMetrics;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_enrollment.enums.EnrollmentStatus;
import com.japaneselearning.module_quiz.dto.*;
import com.japaneselearning.module_quiz.entity.*;
import com.japaneselearning.module_quiz.enums.QuestionType;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.*;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import com.japaneselearning.module_leaderboard.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ScoringService scoringService;
    private final ObservabilityMetrics observabilityMetrics;

    // ==========================================
    // GET QUIZ FOR STUDENT
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public QuizLearningRes getQuizForStudent(Long quizId) {
        Quiz quiz = getPublishedQuiz(quizId);
        validateQuizAccess(quiz);

        List<Question> questions = questionRepository.findByQuizIdOrderBySortOrderAsc(quizId);

        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        Map<Long, List<Answer>> answersByQuestionId = questionIds.isEmpty() ? Map.of() :
                answerRepository.findByQuestionIdInOrderBySortOrderAsc(questionIds).stream()
                        .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        List<QuestionLearningRes> questionResList = questions.stream().map(q -> {
            List<AnswerLearningRes> answerResList = answersByQuestionId
                    .getOrDefault(q.getId(), List.of())
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

        // Calculate next attempt number
        Integer maxAttemptNumber = attemptRepository.findMaxAttemptNumberByUserIdAndQuizId(user.getId(), quizId);
        int nextAttemptNumber = (maxAttemptNumber == null ? 0 : maxAttemptNumber) + 1;

        if (quiz.getMaxAttempts() != null && quiz.getMaxAttempts() > 0) {
            if (nextAttemptNumber > quiz.getMaxAttempts()) {
                throw new AppException(ErrorCode.QUIZ_MAX_ATTEMPTS_REACHED);
            }
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .attemptNumber(nextAttemptNumber)
                .startedAt(LocalDateTime.now())
                .status(QuizAttemptStatus.IN_PROGRESS)
                .build();

        QuizAttempt saved;
        try {
            saved = attemptRepository.saveAndFlush(attempt);
        } catch (DataIntegrityViolationException e) {
            // A concurrent request already created this attempt number
            // We can treat this as reaching the max attempts or concurrency conflict
            throw new AppException(ErrorCode.QUIZ_MAX_ATTEMPTS_REACHED);
        }

        long remainingAttempts = -1; // unlimited
        if (quiz.getMaxAttempts() != null && quiz.getMaxAttempts() > 0) {
            remainingAttempts = quiz.getMaxAttempts() - nextAttemptNumber;
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

        // Time limit check
        if (quiz.getTimeLimitMinutes() != null && quiz.getTimeLimitMinutes() > 0) {
            // Add a 30-second grace period for network latency
            LocalDateTime deadline = attempt.getStartedAt()
                    .plusMinutes(quiz.getTimeLimitMinutes())
                    .plusSeconds(30);
                    
            if (LocalDateTime.now().isAfter(deadline)) {
                attempt.setStatus(QuizAttemptStatus.EXPIRED);
                attemptRepository.save(attempt);
                throw new AppException(ErrorCode.QUIZ_ATTEMPT_EXPIRED);
            }
        }

        // Fetch all questions and build answer lookup
        List<Question> questions = questionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
        Set<Long> quizQuestionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());

        // Validate submitted answers:
        // 1. Cannot be null or empty
        // 2. Prevent duplicate payload manipulation
        // 3. Ensure every submitted questionId belongs to the current quiz
        if (req.getAnswers() == null || req.getAnswers().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Danh sách câu trả lời không được rỗng");
        }

        java.util.Set<Long> seenQuestionIds = new java.util.HashSet<>();
        for (QuizSubmitAnswerReq ansReq : req.getAnswers()) {
            if (ansReq.getQuestionId() == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "questionId không được để trống");
            }
            if (!quizQuestionIds.contains(ansReq.getQuestionId())) {
                throw new AppException(ErrorCode.INVALID_REQUEST,
                        "Câu hỏi không thuộc bài kiểm tra này: " + ansReq.getQuestionId());
            }
            if (!seenQuestionIds.add(ansReq.getQuestionId())) {
                throw new AppException(ErrorCode.INVALID_REQUEST,
                        "Dữ liệu nộp bài chứa câu hỏi trùng lặp: " + ansReq.getQuestionId());
            }
        }

        // Pre-fetch all answers for this quiz's questions for scoring
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        Map<Long, List<Answer>> answersByQuestion = questionIds.isEmpty() ? Map.of() :
                answerRepository.findByQuestionIdInOrderBySortOrderAsc(questionIds).stream()
                        .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        // Build a map from submitted answers: questionId -> QuizSubmitAnswerReq
        Map<Long, QuizSubmitAnswerReq> submittedMap = req.getAnswers().stream()
                .collect(Collectors.toMap(QuizSubmitAnswerReq::getQuestionId, a -> a));

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
            BigDecimal maxPoints = question.getPoints() != null ? question.getPoints() : BigDecimal.ONE;

            if (submitted != null) {
                QuestionType type = question.getQuestionType();

                if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.TRUE_FALSE) {
                    List<Long> effectiveIds = submitted.getEffectiveAnswerIds();
                    Long chosenId = !effectiveIds.isEmpty() ? effectiveIds.get(0) : submitted.getAnswerId();

                    if (chosenId != null) {
                        selectedAnswer = questionAnswers.stream()
                                .filter(a -> a.getId().equals(chosenId))
                                .findFirst().orElse(null);

                        if (selectedAnswer != null && Boolean.TRUE.equals(selectedAnswer.getIsCorrect())) {
                            isCorrect = true;
                            pointsEarned = maxPoints;
                        }
                    }
                } else if (type == QuestionType.MULTIPLE_CHOICE) {
                    List<Long> effectiveIds = submitted.getEffectiveAnswerIds();
                    java.util.Set<Long> selectedIds = new java.util.HashSet<>(effectiveIds);

                    List<Answer> correctAnswers = questionAnswers.stream()
                            .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                            .toList();
                    List<Answer> wrongAnswers = questionAnswers.stream()
                            .filter(a -> !Boolean.TRUE.equals(a.getIsCorrect()))
                            .toList();

                    java.util.Set<Long> correctIds = correctAnswers.stream()
                            .map(Answer::getId).collect(Collectors.toSet());
                    java.util.Set<Long> wrongIds = wrongAnswers.stream()
                            .map(Answer::getId).collect(Collectors.toSet());

                    List<Answer> selectedAnswerObjects = questionAnswers.stream()
                            .filter(a -> selectedIds.contains(a.getId()))
                            .toList();

                    if (selectedAnswerObjects.size() == 1) {
                        selectedAnswer = selectedAnswerObjects.get(0);
                    }
                    if (!selectedAnswerObjects.isEmpty()) {
                        userAnswerText = selectedAnswerObjects.stream()
                                .map(Answer::getContent)
                                .collect(Collectors.joining(", "));
                    }

                    if (!selectedIds.isEmpty() && !correctIds.isEmpty()) {
                        long correctSelected = selectedIds.stream().filter(correctIds::contains).count();
                        long wrongSelected = selectedIds.stream().filter(wrongIds::contains).count();

                        // Exact match
                        if (correctSelected == correctIds.size() && wrongSelected == 0) {
                            isCorrect = true;
                            pointsEarned = maxPoints;
                        } else if (correctSelected > 0) {
                            // Partial credit with penalty for wrong selections:
                            // ratio = max(0, (correctSelected - wrongSelected) / totalCorrect)
                            double ratio = Math.max(0.0, (double) (correctSelected - wrongSelected) / (double) correctIds.size());
                            pointsEarned = maxPoints.multiply(BigDecimal.valueOf(ratio))
                                    .setScale(2, java.math.RoundingMode.HALF_UP);
                            isCorrect = pointsEarned.compareTo(maxPoints) == 0;
                        }
                    }
                } else if (type == QuestionType.FILL_BLANK) {
                    userAnswerText = submitted.getUserAnswerText();
                    if (userAnswerText != null && !userAnswerText.trim().isEmpty()) {
                        String normalizedUser = normalizeText(userAnswerText);
                        boolean matched = questionAnswers.stream()
                                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                                .anyMatch(a -> normalizeText(a.getContent()).equals(normalizedUser));

                        if (matched) {
                            isCorrect = true;
                            pointsEarned = maxPoints;
                        }
                    }
                } else {
                    // MATCHING, LISTENING, REORDER
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

        // Record quiz submission metric for monitoring
        observabilityMetrics.recordQuizSubmission(quizId, Boolean.TRUE.equals(attempt.getPassed()));
        
        if (Boolean.TRUE.equals(attempt.getPassed())) {
            scoringService.addXp(user.getId(), "QUIZ_PASSED", 200, quiz.getId(), "Vượt qua bài kiểm tra: " + quiz.getTitle());
        }

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

        List<Quiz> validQuizzes = quizzes.stream()
                .filter(quiz -> {
                    try {
                        validateQuizAccess(quiz);
                        return true;
                    } catch (AppException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        if (validQuizzes.isEmpty()) {
            return List.of();
        }

        List<Long> quizIds = validQuizzes.stream().map(Quiz::getId).collect(Collectors.toList());

        // Batch fetch question counts per quiz
        Map<Long, Integer> questionCountMap = questionRepository.countQuestionsByQuizIds(quizIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> ((Number) r[1]).intValue()
                ));

        // Batch fetch used attempt counts per quiz
        Map<Long, Long> usedAttemptsMap = attemptRepository.countAttemptsByUserIdAndQuizIds(user.getId(), quizIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> ((Number) r[1]).longValue()
                ));

        // Batch fetch latest attempt per quiz (ordered by startedAt DESC)
        Map<Long, QuizAttempt> latestAttemptMap = new java.util.HashMap<>();
        for (QuizAttempt qa : attemptRepository.findByUserIdAndQuizIdInOrderByStartedAtDesc(user.getId(), quizIds)) {
            latestAttemptMap.putIfAbsent(qa.getQuiz().getId(), qa);
        }

        return validQuizzes.stream()
                .map(quiz -> {
                    int questionCount = questionCountMap.getOrDefault(quiz.getId(), 0);
                    QuizAttempt latestAttempt = latestAttemptMap.get(quiz.getId());

                    long remainingAttempts = -1; // unlimited
                    if (quiz.getMaxAttempts() != null && quiz.getMaxAttempts() > 0) {
                        long usedAttempts = usedAttemptsMap.getOrDefault(quiz.getId(), 0L);
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
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                user.getId(), course.getId(), List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED));
        if (!isEnrolled) {
            throw new AppException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private QuizResultRes buildResultRes(QuizAttempt attempt, Quiz quiz) {
        List<QuizAttemptAnswer> attemptAnswers = attemptAnswerRepository.findByAttemptId(attempt.getId());

        List<Long> questionIds = attemptAnswers.stream()
                .map(aa -> aa.getQuestion().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, List<Answer>> answersByQuestionId = questionIds.isEmpty() ? Map.of() :
                answerRepository.findByQuestionIdInOrderBySortOrderAsc(questionIds).stream()
                        .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        // Build correct answer lookup per question
        Map<Long, Answer> correctAnswerMap = new java.util.HashMap<>();
        for (Long qId : questionIds) {
            Answer correctAnswer = answersByQuestionId.getOrDefault(qId, List.of()).stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .findFirst()
                    .orElse(null);
            if (correctAnswer != null) {
                correctAnswerMap.put(qId, correctAnswer);
            }
        }

        List<QuizResultAnswerRes> answerResList = attemptAnswers.stream().map(aa -> {
            Question q = aa.getQuestion();
            List<Answer> qAnswers = answersByQuestionId.getOrDefault(q.getId(), List.of());
            List<Answer> correctAnswers = qAnswers.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .toList();

            List<Long> correctAnswerIds = correctAnswers.stream().map(Answer::getId).toList();
            String correctAnswerContent = correctAnswers.stream()
                    .map(Answer::getContent)
                    .collect(Collectors.joining(", "));
            Long firstCorrectAnswerId = correctAnswers.isEmpty() ? null : correctAnswers.get(0).getId();

            String selectedAnswerContent = aa.getAnswer() != null ? aa.getAnswer().getContent() : null;
            String userAnswerText = aa.getUserAnswerText();
            if (selectedAnswerContent == null && userAnswerText != null) {
                selectedAnswerContent = userAnswerText;
            }

            List<Long> selectedAnswerIds = null;
            if (aa.getAnswer() != null) {
                selectedAnswerIds = List.of(aa.getAnswer().getId());
            }

            return QuizResultAnswerRes.builder()
                    .questionId(q.getId())
                    .questionContent(q.getContent())
                    .questionType(q.getQuestionType() != null ? q.getQuestionType().name() : null)
                    .selectedAnswerId(aa.getAnswer() != null ? aa.getAnswer().getId() : null)
                    .selectedAnswerIds(selectedAnswerIds)
                    .selectedAnswerContent(selectedAnswerContent)
                    .correctAnswerId(firstCorrectAnswerId)
                    .correctAnswerIds(correctAnswerIds)
                    .correctAnswerContent(correctAnswerContent)
                    .userAnswerText(userAnswerText)
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

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
