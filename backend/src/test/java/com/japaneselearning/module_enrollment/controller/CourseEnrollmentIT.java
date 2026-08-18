package com.japaneselearning.module_enrollment.controller;

import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import com.japaneselearning.module_course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseEnrollmentIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseEnrollmentRepository enrollmentRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private UserRepository userRepository;

    private User student;
    private Course freeCourse;

    @BeforeEach
    void setUp() {
        student = User.builder().id(1L).email("student@example.com").build();
        freeCourse = Course.builder().id(1L).courseType(CourseType.FREE).status(CourseStatus.PUBLISHED).build();
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void enrollFreeCourse_DuplicateEnrollment_ReturnsBusinessError() throws Exception {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(freeCourse));
        
        // Return false on check to simulate race condition bypass
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(false);
        
        // Throw exception on saveAndFlush
        when(enrollmentRepository.saveAndFlush(any(CourseEnrollment.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        mockMvc.perform(post("/api/v1/courses/1/enroll")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_ENROLLED.getCode()));
    }
}
