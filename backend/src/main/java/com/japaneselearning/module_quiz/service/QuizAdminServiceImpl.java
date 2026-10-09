package com.japaneselearning.module_quiz.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_quiz.dto.AnswerCreateReq;
import com.japaneselearning.module_quiz.dto.AnswerRes;
import com.japaneselearning.module_quiz.dto.AnswerUpdateReq;
import com.japaneselearning.module_quiz.dto.QuestionCreateReq;
import com.japaneselearning.module_quiz.dto.QuestionRes;
import com.japaneselearning.module_quiz.dto.QuestionUpdateReq;
import com.japaneselearning.module_quiz.dto.QuizCreateReq;
import com.japaneselearning.module_quiz.dto.QuizRes;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizAdminServiceImpl implements QuizAdminService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    // ==========================================
    // QUIZ MANAGEMENT
    // ==========================================

    @Override
    @Transactional
    public QuizRes createQuiz(QuizCreateReq req) {
        Course course = null;
        Lesson lesson = null;

        if (req.getCourseId() == null && req.getLessonId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (req.getLessonId() != null) {
            lesson = lessonRepository.findById(req.getLessonId())
                    .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
            course = lesson.getCourse();
        } else if (req.getCourseId() != null) {
            course = courseRepository.findById(req.getCourseId())
                    .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        }

        checkDataIsolation(course);

        Quiz quiz = Quiz.builder()
                .course(course)
                .lesson(lesson)
                .title(req.getTitle())
                .description(req.getDescription())
                .timeLimitMinutes(req.getTimeLimitMinutes())
                .passingScore(req.getPassingScore())
                .maxAttempts(req.getMaxAttempts())
                .status(QuizStatus.DRAFT)
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToQuizRes(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizRes getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        checkDataIsolation(quiz.getCourse());
        return mapToQuizRes(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizRes> getQuizzes(Long courseId, Long lessonId, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        String currentUserEmail = auth.getName();

        Page<Quiz> quizPage;

        if (lessonId != null) {
            quizPage = quizRepository.findByLessonId(lessonId, pageable);
            if (!quizPage.isEmpty()) {
                checkDataIsolation(quizPage.getContent().get(0).getCourse());
            }
        } else if (courseId != null) {
            quizPage = quizRepository.findByCourseId(courseId, pageable);
            if (!quizPage.isEmpty()) {
                checkDataIsolation(quizPage.getContent().get(0).getCourse());
            }
        } else {
            if (isAdminOrSuperAdmin) {
                quizPage = quizRepository.findAll(pageable);
            } else {
                quizPage = quizRepository.findByCourseTeacherEmail(currentUserEmail, pageable);
            }
        }

        return quizPage.map(this::mapToQuizRes);
    }

    @Override
    @Transactional
    public QuizRes updateQuiz(Long id, QuizUpdateReq req) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        checkDataIsolation(quiz.getCourse());

        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        quiz.setTimeLimitMinutes(req.getTimeLimitMinutes());
        quiz.setPassingScore(req.getPassingScore());
        quiz.setMaxAttempts(req.getMaxAttempts());
        quiz.setStatus(req.getStatus());

        return mapToQuizRes(quizRepository.save(quiz));
    }

    @Override
    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        checkDataIsolation(quiz.getCourse());

        if (quizAttemptRepository.existsByQuizId(id)) {
            throw new AppException(ErrorCode.QUIZ_HAS_ATTEMPT);
        }

        quiz.setStatus(QuizStatus.ARCHIVED);
        quizRepository.save(quiz);
    }

    /**
     * Các loại câu hỏi mà hệ thống hỗ trợ chấm điểm tự động.
     */
    private static final EnumSet<QuestionType> SUPPORTED_QUESTION_TYPES = EnumSet.of(
            QuestionType.SINGLE_CHOICE,
            QuestionType.TRUE_FALSE,
            QuestionType.MULTIPLE_CHOICE
    );

    @Override
    @Transactional
    public QuizRes publishQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        checkDataIsolation(quiz.getCourse());

        List<Question> questions = questionRepository.findByQuizIdOrderBySortOrderAsc(id);
        if (questions.isEmpty()) {
            throw new AppException(ErrorCode.QUIZ_PUBLISH_NO_QUESTION);
        }

        // Pre-fetch tất cả answer của các question trong quiz
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        Map<Long, List<Answer>> answersByQuestion = answerRepository
                .findByQuestionIdInOrderBySortOrderAsc(questionIds).stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        // Validate từng câu hỏi
        for (Question q : questions) {
            List<Answer> answers = answersByQuestion.getOrDefault(q.getId(), List.of());
            validateQuestionForPublish(q, answers);
        }

        quiz.setStatus(QuizStatus.PUBLISHED);
        return mapToQuizRes(quizRepository.save(quiz));
    }

    @Override
    @Transactional
    public QuizRes hideQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        checkDataIsolation(quiz.getCourse());

        quiz.setStatus(QuizStatus.HIDDEN);
        return mapToQuizRes(quizRepository.save(quiz));
    }

    // ==========================================
    // QUESTION MANAGEMENT
    // ==========================================

    @Override
    @Transactional
    public QuestionRes createQuestion(Long quizId, QuestionCreateReq req) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        checkDataIsolation(quiz.getCourse());

        Question question = Question.builder()
                .quiz(quiz)
                .questionType(req.getQuestionType())
                .content(req.getContent())
                .audioUrl(req.getAudioUrl())
                .imageUrl(req.getImageUrl())
                .explanation(req.getExplanation())
                .points(req.getPoints())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .build();

        return mapToQuestionRes(questionRepository.save(question));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionRes> getQuestionsByQuizId(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        checkDataIsolation(quiz.getCourse());

        return questionRepository.findByQuizIdOrderBySortOrderAsc(quizId).stream()
                .map(this::mapToQuestionRes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionRes getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        checkDataIsolation(question.getQuiz().getCourse());
        return mapToQuestionRes(question);
    }

    @Override
    @Transactional
    public QuestionRes updateQuestion(Long id, QuestionUpdateReq req) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        checkDataIsolation(question.getQuiz().getCourse());

        if (quizAttemptAnswerRepository.existsByQuestionId(id)) {
            throw new AppException(ErrorCode.QUESTION_HAS_ATTEMPT);
        }

        question.setContent(req.getContent());
        question.setAudioUrl(req.getAudioUrl());
        question.setImageUrl(req.getImageUrl());
        question.setExplanation(req.getExplanation());
        question.setPoints(req.getPoints());

        if (req.getSortOrder() != null) {
            question.setSortOrder(req.getSortOrder());
        }

        return mapToQuestionRes(questionRepository.save(question));
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        checkDataIsolation(question.getQuiz().getCourse());

        if (quizAttemptAnswerRepository.existsByQuestionId(id)) {
            throw new AppException(ErrorCode.QUESTION_HAS_ATTEMPT);
        }

        answerRepository.deleteByQuestionId(id);
        questionRepository.delete(question);
    }

    // ==========================================
    // ANSWER MANAGEMENT
    // ==========================================

    @Override
    @Transactional
    public AnswerRes createAnswer(Long questionId, AnswerCreateReq req) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        checkDataIsolation(question.getQuiz().getCourse());

        Answer answer = Answer.builder()
                .question(question)
                .content(req.getContent())
                .isCorrect(req.getIsCorrect())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .build();

        return mapToAnswerRes(answerRepository.save(answer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerRes> getAnswersByQuestionId(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        checkDataIsolation(question.getQuiz().getCourse());

        return answerRepository.findByQuestionIdOrderBySortOrderAsc(questionId).stream()
                .map(this::mapToAnswerRes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnswerRes updateAnswer(Long id, AnswerUpdateReq req) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));

        checkDataIsolation(answer.getQuestion().getQuiz().getCourse());

        if (quizAttemptAnswerRepository.existsByQuestionId(answer.getQuestion().getId())) {
            throw new AppException(ErrorCode.QUESTION_HAS_ATTEMPT);
        }

        answer.setContent(req.getContent());
        answer.setIsCorrect(req.getIsCorrect());

        if (req.getSortOrder() != null) {
            answer.setSortOrder(req.getSortOrder());
        }

        return mapToAnswerRes(answerRepository.save(answer));
    }

    @Override
    @Transactional
    public void deleteAnswer(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));

        checkDataIsolation(answer.getQuestion().getQuiz().getCourse());

        if (quizAttemptAnswerRepository.existsByQuestionId(answer.getQuestion().getId())) {
            throw new AppException(ErrorCode.QUESTION_HAS_ATTEMPT);
        }

        answerRepository.delete(answer);
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    /**
     * Kiểm tra cấu trúc câu hỏi và đáp án trước khi publish quiz.
     * Quy tắc:
     * - SINGLE_CHOICE: ≥ 2 đáp án, đúng 1 đáp án đúng
     * - TRUE_FALSE: đúng 2 đáp án, đúng 1 đáp án đúng
     * - MULTIPLE_CHOICE: ≥ 2 đáp án, ≥ 1 đáp án đúng
     * - Các loại khác (FILL_BLANK, MATCHING, LISTENING, REORDER): chặn publish
     */
    private void validateQuestionForPublish(Question question, List<Answer> answers) {
        QuestionType type = question.getQuestionType();
        String label = question.getContent();
        // Cắt label cho gọn nếu quá dài
        if (label != null && label.length() > 50) {
            label = label.substring(0, 50) + "...";
        }

        // Chặn loại câu hỏi chưa hỗ trợ chấm điểm tự động
        if (!SUPPORTED_QUESTION_TYPES.contains(type)) {
            throw new AppException(ErrorCode.QUIZ_PUBLISH_UNSUPPORTED_QUESTION_TYPE,
                    String.format(ErrorCode.QUIZ_PUBLISH_UNSUPPORTED_QUESTION_TYPE.getMessage(),
                            label, type.name()));
        }

        // Kiểm tra câu hỏi phải có đáp án
        if (answers.isEmpty()) {
            throw new AppException(ErrorCode.QUIZ_PUBLISH_QUESTION_NO_ANSWER,
                    String.format(ErrorCode.QUIZ_PUBLISH_QUESTION_NO_ANSWER.getMessage(), label));
        }

        long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();

        switch (type) {
            case SINGLE_CHOICE -> {
                if (answers.size() < 2) {
                    throw new AppException(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER,
                            String.format(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getMessage(), label)
                                    + ": cần ít nhất 2 đáp án");
                }
                if (correctCount != 1) {
                    throw new AppException(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER,
                            String.format(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getMessage(), label)
                                    + ": cần đúng 1 đáp án đúng, hiện có " + correctCount);
                }
            }
            case TRUE_FALSE -> {
                if (answers.size() != 2) {
                    throw new AppException(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER,
                            String.format(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getMessage(), label)
                                    + ": cần đúng 2 đáp án, hiện có " + answers.size());
                }
                if (correctCount != 1) {
                    throw new AppException(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER,
                            String.format(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getMessage(), label)
                                    + ": cần đúng 1 đáp án đúng, hiện có " + correctCount);
                }
            }
            case MULTIPLE_CHOICE -> {
                if (answers.size() < 2) {
                    throw new AppException(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER,
                            String.format(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getMessage(), label)
                                    + ": cần ít nhất 2 đáp án");
                }
                if (correctCount < 1) {
                    throw new AppException(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER,
                            String.format(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getMessage(), label)
                                    + ": cần ít nhất 1 đáp án đúng");
                }
            }
            default -> { /* Already handled by SUPPORTED_QUESTION_TYPES check above */ }
        }
    }

    private void checkDataIsolation(Course course) {
        if (course == null)
            return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdminOrSuperAdmin) {
            if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
                throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
            }
        }
    }

    private QuizRes mapToQuizRes(Quiz quiz) {
        return QuizRes.builder()
                .id(quiz.getId())
                .courseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null)
                .lessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null)
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .passingScore(quiz.getPassingScore())
                .maxAttempts(quiz.getMaxAttempts())
                .status(quiz.getStatus() != null ? quiz.getStatus().name() : null)
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private QuestionRes mapToQuestionRes(Question question) {
        return QuestionRes.builder()
                .id(question.getId())
                .quizId(question.getQuiz() != null ? question.getQuiz().getId() : null)
                .questionType(question.getQuestionType() != null ? question.getQuestionType().name() : null)
                .content(question.getContent())
                .audioUrl(question.getAudioUrl())
                .imageUrl(question.getImageUrl())
                .explanation(question.getExplanation())
                .points(question.getPoints())
                .sortOrder(question.getSortOrder())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    private AnswerRes mapToAnswerRes(Answer answer) {
        return AnswerRes.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion() != null ? answer.getQuestion().getId() : null)
                .content(answer.getContent())
                .isCorrect(answer.getIsCorrect())
                .sortOrder(answer.getSortOrder())
                .createdAt(answer.getCreatedAt())
                .build();
    }
}
