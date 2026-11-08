package com.japaneselearning.module_user.controller;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_learning.repository.LessonProgressRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentDashboardIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseEnrollmentRepository enrollmentRepository;

    @MockBean
    private LessonProgressRepository progressRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private UserRepository userRepository;

    private User student;

    @BeforeEach
    void setUp() {
        student = User.builder().id(1L).email("student@example.com").build();
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getMyProgressOverview_ScopedToEnrolledCourses() throws Exception {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        
        Course course1 = Course.builder().id(101L).totalLessons(5).build();
        Course course2 = Course.builder().id(102L).totalLessons(5).build();
        CourseEnrollment enrollment1 = CourseEnrollment.builder().course(course1).build();
        CourseEnrollment enrollment2 = CourseEnrollment.builder().course(course2).build();

        // User is enrolled in course 101 and 102
        when(enrollmentRepository.findByUserId(1L))
                .thenReturn(List.of(enrollment1, enrollment2));
        when(enrollmentRepository.findByUserIdAndStatusIn(eq(1L), any()))
                .thenReturn(List.of(enrollment1, enrollment2));

        // Mock completed lessons for this user in these specific courses
        when(progressRepository.countByUserIdAndLessonCourseIdInAndIsCompletedTrue(1L, List.of(101L, 102L)))
                .thenReturn(4);

        mockMvc.perform(get("/api/users/me/progress")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.totalEnrolledCourses").value(2))
                .andExpect(jsonPath("$.result.totalCompletedLessons").value(4))
                .andExpect(jsonPath("$.result.overallProgressPercent").value(40.0));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getMyCourses_OnlyReturnsAccessibleEnrollments() throws Exception {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));

        Course course1 = Course.builder().id(101L).title("Course 1").slug("course-1").totalLessons(5).build();
        Course course2 = Course.builder().id(102L).title("Course 2").slug("course-2").totalLessons(5).build();
        CourseEnrollment activeEnrollment = CourseEnrollment.builder().course(course1).status(com.japaneselearning.module_enrollment.enums.EnrollmentStatus.ACTIVE).build();
        CourseEnrollment completedEnrollment = CourseEnrollment.builder().course(course2).status(com.japaneselearning.module_enrollment.enums.EnrollmentStatus.COMPLETED).build();

        // Repository filters by accessible statuses (ACTIVE, COMPLETED), excluding PAUSED and CANCELLED
        when(enrollmentRepository.findByUserIdAndStatusIn(eq(1L), any()))
                .thenReturn(List.of(activeEnrollment, completedEnrollment));
        when(progressRepository.countCompletedLessonsByCourseForUser(eq(1L), any()))
                .thenReturn(List.of());
        when(progressRepository.findLatestProgressForEachCourseByUserId(eq(1L), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/users/me/courses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].courseId").value(101))
                .andExpect(jsonPath("$.result[1].courseId").value(102));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getMyCourses_WhenOnlyPausedOrCancelled_ReturnsEmptyList() throws Exception {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));

        // When user has only PAUSED or CANCELLED enrollments, accessible query returns empty
        when(enrollmentRepository.findByUserIdAndStatusIn(eq(1L), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/users/me/courses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.length()").value(0));
    }
}
