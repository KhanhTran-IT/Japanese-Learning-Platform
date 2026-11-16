package com.japaneselearning.module_quiz.service;

import com.japaneselearning.common.response.PageResponse;
import com.japaneselearning.module_quiz.dto.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizLearningService {

    /** Get published quiz detail for student (hides isCorrect). */
    QuizLearningRes getQuizForStudent(Long quizId);

    /** Start a new attempt for a quiz. Checks maxAttempts. */
    QuizAttemptStartRes startAttempt(Long quizId);

    /** Submit answers for an attempt. Scores and saves results. */
    QuizResultRes submitAttempt(Long quizId, QuizSubmitReq req);

    /** Get result of a specific attempt. Only owner or admin/super admin. */
    QuizResultRes getAttemptResult(Long quizId, Long attemptId);

    /** List paginated quiz attempts for current user. */
    PageResponse<QuizAttemptSummaryRes> getMyAttempts(Pageable pageable);

    /** List default paginated quiz attempts for current user (first page, size 10). */
    default PageResponse<QuizAttemptSummaryRes> getMyAttempts() {
        return getMyAttempts(PageRequest.of(0, 10));
    }

    /** Discover published quizzes for a lesson. */
    List<QuizDiscoveryRes> getLessonQuizzes(Long lessonId);
}
