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

import static org.mockito.ArgumentMatchers.anyLong;
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


        
        // Mock completed lessons for this user in these specific courses
        when(progressRepository.countByUserIdAndLessonCourseIdInAndIsCompletedTrue(1L, List.of(101L, 102L)))
                .thenReturn(4);

        mockMvc.perform(get("/api/student/dashboard/progress")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.totalEnrolledCourses").value(2))
                .andExpect(jsonPath("$.result.totalCompletedLessons").value(4))
                .andExpect(jsonPath("$.result.overallProgressPercent").value(40.0));
    }
}
