package com.japaneselearning.module_quiz.service;

import com.japaneselearning.module_quiz.dto.*;

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

    /** List all quiz attempts for current user. */
    List<QuizAttemptSummaryRes> getMyAttempts();
}
