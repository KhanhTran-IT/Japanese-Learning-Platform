package com.japaneselearning.module_quiz.controller;

import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_quiz.entity.Answer;
import com.japaneselearning.module_quiz.entity.Question;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.enums.QuestionType;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.AnswerRepository;
import com.japaneselearning.module_quiz.repository.QuestionRepository;
import com.japaneselearning.module_quiz.repository.QuizRepository;
import com.japaneselearning.module_user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizPublishValidationIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizRepository quizRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private AnswerRepository answerRepository;

    private Quiz quiz;
    private Question singleChoiceQuestion;

    @BeforeEach
    void setUp() {
        User teacher = new User();
        teacher.setEmail("teacher@example.com");

        Course course = new Course();
        course.setId(1L);
        course.setTeacher(teacher);

        quiz = Quiz.builder()
                .id(100L)
                .title("Test Quiz")
                .status(QuizStatus.DRAFT)
                .course(course)
                .build();

        singleChoiceQuestion = Question.builder()
                .id(1L)
                .quiz(quiz)
                .questionType(QuestionType.SINGLE_CHOICE)
                .content("Hiragana の読み方は？")
                .build();

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void publishQuiz_QuestionWithNoAnswers_Fails() throws Exception {
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L))
                .thenReturn(List.of(singleChoiceQuestion));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(1L)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(put("/api/v1/admin/quizzes/100/publish")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.QUIZ_PUBLISH_QUESTION_NO_ANSWER.getCode()));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void publishQuiz_SingleChoiceNoCorrectAnswer_Fails() throws Exception {
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L))
                .thenReturn(List.of(singleChoiceQuestion));

        List<Answer> answers = List.of(
                Answer.builder().id(10L).question(singleChoiceQuestion).content("A").isCorrect(false).build(),
                Answer.builder().id(11L).question(singleChoiceQuestion).content("B").isCorrect(false).build()
        );
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(1L)))
                .thenReturn(answers);

        mockMvc.perform(put("/api/v1/admin/quizzes/100/publish")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getCode()));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void publishQuiz_SingleChoiceMultipleCorrect_Fails() throws Exception {
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L))
                .thenReturn(List.of(singleChoiceQuestion));

        List<Answer> answers = List.of(
                Answer.builder().id(10L).question(singleChoiceQuestion).content("A").isCorrect(true).build(),
                Answer.builder().id(11L).question(singleChoiceQuestion).content("B").isCorrect(true).build()
        );
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(1L)))
                .thenReturn(answers);

        mockMvc.perform(put("/api/v1/admin/quizzes/100/publish")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getCode()));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void publishQuiz_TrueFalseWithThreeAnswers_Fails() throws Exception {
        Question tfQuestion = Question.builder()
                .id(2L)
                .quiz(quiz)
                .questionType(QuestionType.TRUE_FALSE)
                .content("東京は日本の首都ですか？")
                .build();

        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L))
                .thenReturn(List.of(tfQuestion));

        List<Answer> answers = List.of(
                Answer.builder().id(20L).question(tfQuestion).content("Đúng").isCorrect(true).build(),
                Answer.builder().id(21L).question(tfQuestion).content("Sai").isCorrect(false).build(),
                Answer.builder().id(22L).question(tfQuestion).content("Không biết").isCorrect(false).build()
        );
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(2L)))
                .thenReturn(answers);

        mockMvc.perform(put("/api/v1/admin/quizzes/100/publish")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.QUIZ_PUBLISH_INVALID_CORRECT_ANSWER.getCode()));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void publishQuiz_UnsupportedType_Fails() throws Exception {
        Question fillBlankQuestion = Question.builder()
                .id(3L)
                .quiz(quiz)
                .questionType(QuestionType.FILL_BLANK)
                .content("___は日本語で「cat」です。")
                .build();

        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L))
                .thenReturn(List.of(fillBlankQuestion));

        // Không cần mock answer vì sẽ bị chặn trước bởi unsupported type check
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(3L)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(put("/api/v1/admin/quizzes/100/publish")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.QUIZ_PUBLISH_UNSUPPORTED_QUESTION_TYPE.getCode()));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void publishQuiz_ValidStructure_Succeeds() throws Exception {
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L))
                .thenReturn(List.of(singleChoiceQuestion));

        List<Answer> answers = List.of(
                Answer.builder().id(10L).question(singleChoiceQuestion).content("あ").isCorrect(true).build(),
                Answer.builder().id(11L).question(singleChoiceQuestion).content("い").isCorrect(false).build(),
                Answer.builder().id(12L).question(singleChoiceQuestion).content("う").isCorrect(false).build()
        );
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(1L)))
                .thenReturn(answers);

        mockMvc.perform(put("/api/v1/admin/quizzes/100/publish")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}
