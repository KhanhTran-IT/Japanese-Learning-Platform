package com.japaneselearning.module_quiz.controller;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.repository.QuizRepository;
import com.japaneselearning.module_user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizAdminListingIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizRepository quizRepository;

    private Quiz mockQuiz;
    private Page<Quiz> mockPage;

    @BeforeEach
    void setUp() {
        User teacher = new User();
        teacher.setEmail("teacher@example.com");

        Course course = new Course();
        course.setId(1L);
        course.setTeacher(teacher);

        mockQuiz = Quiz.builder()
                .id(100L)
                .title("Test Quiz")
                .course(course)
                .build();

        mockPage = new PageImpl<>(List.of(mockQuiz), PageRequest.of(0, 10), 1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQuizzes_AsAdmin_ReturnsAllQuizzes() throws Exception {
        when(quizRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/admin/quizzes")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(100L));

        verify(quizRepository).findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void getQuizzes_AsTeacher_ReturnsTeacherQuizzes() throws Exception {
        when(quizRepository.findByCourseTeacherEmail(eq("teacher@example.com"), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/admin/quizzes")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(100L));

        verify(quizRepository).findByCourseTeacherEmail(eq("teacher@example.com"), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void getQuizzes_ByCourseId_ReturnsCourseQuizzesAndChecksIsolation() throws Exception {
        when(quizRepository.findByCourseId(eq(1L), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/admin/quizzes")
                .param("courseId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(100L));

        verify(quizRepository).findByCourseId(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "other@example.com", roles = "TEACHER")
    void getQuizzes_ByCourseId_FailsDataIsolation() throws Exception {
        when(quizRepository.findByCourseId(eq(1L), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/admin/quizzes")
                .param("courseId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void getQuizzes_ByLessonId_ReturnsLessonQuizzes() throws Exception {
        when(quizRepository.findByLessonId(eq(2L), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/admin/quizzes")
                .param("lessonId", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(100L));

        verify(quizRepository).findByLessonId(eq(2L), any(Pageable.class));
    }
}
