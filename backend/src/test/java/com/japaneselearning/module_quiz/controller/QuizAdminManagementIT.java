package com.japaneselearning.module_quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_quiz.dto.QuestionCreateReq;
import com.japaneselearning.module_quiz.dto.QuestionUpdateReq;
import com.japaneselearning.module_quiz.dto.QuizCreateReq;
import com.japaneselearning.module_quiz.dto.QuizUpdateReq;
import com.japaneselearning.module_quiz.entity.Question;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.enums.QuestionType;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import com.japaneselearning.module_quiz.repository.AnswerRepository;
import com.japaneselearning.module_quiz.repository.QuestionRepository;
import com.japaneselearning.module_quiz.repository.QuizAttemptAnswerRepository;
import com.japaneselearning.module_quiz.repository.QuizAttemptRepository;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizAdminManagementIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizRepository quizRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private AnswerRepository answerRepository;

    @MockBean
    private QuizAttemptRepository quizAttemptRepository;

    @MockBean
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    private User teacher1;
    private User teacher2;
    private Course course1;
    private Course course2;
    private Quiz quiz;
    private Question question;

    @BeforeEach
    void setUp() {
        teacher1 = new User();
        teacher1.setEmail("teacher1@example.com");

        teacher2 = new User();
        teacher2.setEmail("teacher2@example.com");

        course1 = new Course();
        course1.setId(1L);
        course1.setTeacher(teacher1);

        course2 = new Course();
        course2.setId(2L);
        course2.setTeacher(teacher2);

        quiz = Quiz.builder()
                .id(100L)
                .title("Test Quiz")
                .status(QuizStatus.DRAFT)
                .course(course1)
                .build();
                
        question = Question.builder()
                .id(50L)
                .quiz(quiz)
                .questionType(QuestionType.SINGLE_CHOICE)
                .content("Question 1")
                .points(BigDecimal.ONE)
                .sortOrder(1)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course1));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course2));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(questionRepository.findById(50L)).thenReturn(Optional.of(question));
        
        when(quizRepository.save(any(Quiz.class))).thenAnswer(i -> i.getArgument(0));
        when(questionRepository.save(any(Question.class))).thenAnswer(i -> {
            Question q = i.getArgument(0);
            if (q.getId() == null) q.setId(51L);
            return q;
        });
    }

    @Test
    @WithMockUser(username = "teacher1@example.com", roles = "TEACHER")
    void createQuiz_AsTeacher_Succeeds() throws Exception {
        QuizCreateReq req = new QuizCreateReq();
        req.setCourseId(1L);
        req.setTitle("New Quiz");

        mockMvc.perform(post("/api/v1/admin/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.title").value("New Quiz"));
                
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    @WithMockUser(username = "teacher2@example.com", roles = "TEACHER")
    void updateQuiz_FailsDataIsolation() throws Exception {
        QuizUpdateReq req = new QuizUpdateReq();
        req.setTitle("Updated Title");
        req.setStatus(QuizStatus.PUBLISHED);
        req.setMaxAttempts(2);

        mockMvc.perform(put("/api/v1/admin/quizzes/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(3004));
    }

    @Test
    @WithMockUser(username = "teacher1@example.com", roles = "TEACHER")
    void deleteQuiz_WhenHasAttempt_Fails() throws Exception {
        when(quizAttemptRepository.existsByQuizId(100L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/admin/quizzes/100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4015)); // QUIZ_HAS_ATTEMPT
    }

    @Test
    @WithMockUser(username = "teacher1@example.com", roles = "TEACHER")
    void createQuestion_Success_And_MaintainsOrder() throws Exception {
        QuestionCreateReq req = new QuestionCreateReq();
        req.setContent("New Question");
        req.setQuestionType(QuestionType.TRUE_FALSE);
        req.setPoints(BigDecimal.valueOf(2.5));
        req.setSortOrder(5);

        mockMvc.perform(post("/api/v1/admin/quizzes/100/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.sortOrder").value(5))
                .andExpect(jsonPath("$.result.questionType").value("TRUE_FALSE"));
                
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @WithMockUser(username = "teacher1@example.com", roles = "TEACHER")
    void updateQuestion_WhenHasAttempt_Fails() throws Exception {
        when(quizAttemptAnswerRepository.existsByQuestionId(50L)).thenReturn(true);

        QuestionUpdateReq req = new QuestionUpdateReq();
        req.setContent("Updated");

        mockMvc.perform(put("/api/v1/admin/questions/50")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4016)); // QUESTION_HAS_ATTEMPT
    }

    @Test
    @WithMockUser(username = "teacher1@example.com", roles = "TEACHER")
    void hideQuiz_Succeeds() throws Exception {
        mockMvc.perform(put("/api/v1/admin/quizzes/100/hide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.status").value("HIDDEN"));
                
        verify(quizRepository).save(any(Quiz.class));
    }
}
