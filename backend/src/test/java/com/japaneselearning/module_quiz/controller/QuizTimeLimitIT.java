package com.japaneselearning.module_quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_quiz.dto.QuizSubmitAnswerReq;
import com.japaneselearning.module_quiz.dto.QuizSubmitReq;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.entity.QuizAttempt;
import com.japaneselearning.module_quiz.enums.QuizAttemptStatus;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.*;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizTimeLimitIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizRepository quizRepository;

    @MockBean
    private QuizAttemptRepository attemptRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private AnswerRepository answerRepository;

    @MockBean
    private QuizAttemptAnswerRepository attemptAnswerRepository;

    private User student;
    private Quiz quiz;
    private QuizAttempt attempt;

    @BeforeEach
    void setUp() {
        student = User.builder().id(1L).email("student@example.com").build();
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));

        quiz = Quiz.builder()
                .id(100L)
                .title("Test Quiz")
                .status(QuizStatus.PUBLISHED)
                .timeLimitMinutes(10) // 10 minutes limit
                .build();
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));

        attempt = QuizAttempt.builder()
                .id(500L)
                .user(student)
                .quiz(quiz)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusMinutes(5)) // Started 5 mins ago (within limit)
                .build();
        when(attemptRepository.findById(500L)).thenReturn(Optional.of(attempt));

        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L)).thenReturn(Collections.emptyList());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void submitAttempt_WithinTimeLimit_Success() throws Exception {
        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        QuizSubmitAnswerReq ans = new QuizSubmitAnswerReq();
        ans.setQuestionId(1L);
        ans.setAnswerId(2L);
        req.setAnswers(List.of(ans));

        mockMvc.perform(post("/api/v1/quizzes/100/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void submitAttempt_ExceedsTimeLimit_ReturnsExpiredError() throws Exception {
        // Set startedAt to 15 minutes ago (limit is 10) + grace period buffer
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(12));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        QuizSubmitAnswerReq ans = new QuizSubmitAnswerReq();
        ans.setQuestionId(1L);
        ans.setAnswerId(2L);
        req.setAnswers(List.of(ans));

        mockMvc.perform(post("/api/v1/quizzes/100/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.QUIZ_ATTEMPT_EXPIRED.getCode()));

        verify(attemptRepository).save(attempt); // Should save the EXPIRED status
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void submitAttempt_UntimedQuiz_SuccessRegardlessOfTime() throws Exception {
        quiz.setTimeLimitMinutes(null); // Untimed
        attempt.setStartedAt(LocalDateTime.now().minusDays(1)); // Started a day ago

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(500L);
        QuizSubmitAnswerReq ans = new QuizSubmitAnswerReq();
        ans.setQuestionId(1L);
        ans.setAnswerId(2L);
        req.setAnswers(List.of(ans));

        mockMvc.perform(post("/api/v1/quizzes/100/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}
