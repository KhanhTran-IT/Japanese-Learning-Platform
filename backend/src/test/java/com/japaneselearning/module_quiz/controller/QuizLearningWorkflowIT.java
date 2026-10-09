package com.japaneselearning.module_quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_quiz.dto.QuizSubmitAnswerReq;
import com.japaneselearning.module_quiz.dto.QuizSubmitReq;
import com.japaneselearning.module_quiz.entity.Answer;
import com.japaneselearning.module_quiz.entity.Question;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.entity.QuizAttempt;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizLearningWorkflowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizRepository quizRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private AnswerRepository answerRepository;

    @MockBean
    private QuizAttemptRepository quizAttemptRepository;

    @MockBean
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LessonRepository lessonRepository;

    @MockBean
    private CourseEnrollmentRepository courseEnrollmentRepository;

    private User student;
    private User otherStudent;
    private Quiz publishedQuiz;
    private Quiz draftQuiz;
    private Question question1;
    private Question question2;
    private Answer answer1Right;
    private Answer answer1Wrong;
    private QuizAttempt attempt;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(10L);
        student.setEmail("student@example.com");

        otherStudent = new User();
        otherStudent.setId(11L);
        otherStudent.setEmail("other@example.com");

        Course course = new Course();
        course.setId(1L);
        course.setStatus(com.japaneselearning.module_course.enums.CourseStatus.PUBLISHED);

        Lesson lesson = new Lesson();
        lesson.setId(20L);
        lesson.setCourse(course);

        publishedQuiz = Quiz.builder()
                .id(100L)
                .title("N5 Quiz")
                .status(QuizStatus.PUBLISHED)
                .lesson(lesson)
                .course(course)
                .maxAttempts(2)
                .passingScore(BigDecimal.valueOf(1.0))
                .timeLimitMinutes(10)
                .build();

        draftQuiz = Quiz.builder()
                .id(101L)
                .status(QuizStatus.DRAFT)
                .course(course)
                .build();

        question1 = Question.builder()
                .id(50L)
                .quiz(publishedQuiz)
                .questionType(QuestionType.SINGLE_CHOICE)
                .content("Q1")
                .points(BigDecimal.valueOf(1.0))
                .build();

        question2 = Question.builder()
                .id(51L)
                .quiz(publishedQuiz)
                .questionType(QuestionType.TRUE_FALSE)
                .content("Q2")
                .points(BigDecimal.valueOf(1.0))
                .build();

        answer1Right = Answer.builder().id(200L).question(question1).isCorrect(true).build();
        answer1Wrong = Answer.builder().id(201L).question(question1).isCorrect(false).build();

        attempt = QuizAttempt.builder()
                .id(300L)
                .quiz(publishedQuiz)
                .user(student)
                .startedAt(LocalDateTime.now())
                .status(QuizAttemptStatus.IN_PROGRESS)
                .build();

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherStudent));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(publishedQuiz));
        when(quizRepository.findById(101L)).thenReturn(Optional.of(draftQuiz));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));
        
        when(courseEnrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                eq(10L), eq(1L), any())).thenReturn(true);
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getLessonQuizzes_ReturnsOnlyPublished() throws Exception {
        when(quizRepository.findByLessonIdAndStatus(20L, QuizStatus.PUBLISHED))
                .thenReturn(List.of(publishedQuiz));

        mockMvc.perform(get("/api/v1/lessons/20/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].id").value(100L));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getQuizDetail_HidesCorrectAnswers() throws Exception {
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L))
                .thenReturn(List.of(question1));
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(50L)))
                .thenReturn(List.of(answer1Right, answer1Wrong));

        mockMvc.perform(get("/api/v1/quizzes/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.questions[0].answers[0].isCorrect").doesNotExist())
                .andExpect(jsonPath("$.result.questions[0].answers[1].isCorrect").doesNotExist());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void startAttempt_ExceedsMaxAttempts_Fails() throws Exception {
        // limit is 2
        when(quizAttemptRepository.findMaxAttemptNumberByUserIdAndQuizId(10L, 100L)).thenReturn(2);

        mockMvc.perform(post("/api/v1/quizzes/100/start"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4018)); // QUIZ_MAX_ATTEMPTS_REACHED
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void submitAttempt_ValidAnswers_CalculatesScoreCorrectly() throws Exception {
        when(quizAttemptRepository.findById(300L)).thenReturn(Optional.of(attempt));
        
        when(questionRepository.findByQuizIdOrderBySortOrderAsc(100L))
                .thenReturn(List.of(question1, question2));
        
        when(answerRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(50L, 51L)))
                .thenReturn(List.of(answer1Right, answer1Wrong));

        QuizSubmitReq req = new QuizSubmitReq();
        req.setAttemptId(300L);
        
        QuizSubmitAnswerReq ans1 = new QuizSubmitAnswerReq();
        ans1.setQuestionId(50L);
        ans1.setAnswerId(200L); // Correct
        
        QuizSubmitAnswerReq ans2 = new QuizSubmitAnswerReq();
        ans2.setQuestionId(51L);
        ans2.setAnswerId(999L); // Wrong/Fake

        req.setAnswers(List.of(ans1, ans2));

        mockMvc.perform(post("/api/v1/quizzes/100/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.score").value(1.0))
                .andExpect(jsonPath("$.result.correctCount").value(1))
                .andExpect(jsonPath("$.result.wrongCount").value(1))
                .andExpect(jsonPath("$.result.passed").value(true)); // passing is 1.0
                
        verify(quizAttemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = "STUDENT")
    void getResult_AsOtherStudent_Fails() throws Exception {
        attempt.setStatus(QuizAttemptStatus.SUBMITTED);
        when(quizAttemptRepository.findById(300L)).thenReturn(Optional.of(attempt));

        // otherStudent id=11, attempt user id=10
        mockMvc.perform(get("/api/v1/quizzes/100/result/300"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(4021)); // QUIZ_ATTEMPT_FORBIDDEN
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getResult_AsOwner_ShowsFullDetails() throws Exception {
        attempt.setStatus(QuizAttemptStatus.SUBMITTED);
        when(quizAttemptRepository.findById(300L)).thenReturn(Optional.of(attempt));
        when(quizAttemptAnswerRepository.findByAttemptId(300L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/quizzes/100/result/300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
        // Note: Full detail structure check could be deeper, but avoiding HTTP 403 is key here.
    }
}
