package com.japaneselearning.module_quiz.service;

import com.japaneselearning.module_quiz.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizAdminService {

    // Quiz Management
    QuizRes createQuiz(QuizCreateReq req);
    QuizRes getQuizById(Long id);
    Page<QuizRes> getQuizzes(Long courseId, Long lessonId, Pageable pageable);
    QuizRes updateQuiz(Long id, QuizUpdateReq req);
    void deleteQuiz(Long id);
    QuizRes publishQuiz(Long id);
    QuizRes hideQuiz(Long id);

    // Question Management
    QuestionRes createQuestion(Long quizId, QuestionCreateReq req);
    List<QuestionRes> getQuestionsByQuizId(Long quizId);
    QuestionRes getQuestionById(Long id);
    QuestionRes updateQuestion(Long id, QuestionUpdateReq req);
    void deleteQuestion(Long id);

    // Answer Management
    AnswerRes createAnswer(Long questionId, AnswerCreateReq req);
    List<AnswerRes> getAnswersByQuestionId(Long questionId);
    AnswerRes updateAnswer(Long id, AnswerUpdateReq req);
    void deleteAnswer(Long id);
}
