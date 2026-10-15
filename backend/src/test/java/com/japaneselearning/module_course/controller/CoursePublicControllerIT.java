package com.japaneselearning.module_course.controller;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CoursePublicControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private CourseSectionRepository courseSectionRepository;

    @MockBean
    private LessonRepository lessonRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CourseEnrollmentRepository enrollmentRepository;

    @MockBean
    private LessonProgressRepository progressRepository;

    private Course course;
    private User teacher;
    private User student;

    @BeforeEach
    void setUp() {
        teacher = User.builder().id(2L).fullName("Yamada Sensei").build();
        student = User.builder().id(1L).email("student@example.com").build();

        course = Course.builder()
                .id(10L)
                .title("Khóa học N5 Cơ bản")
                .slug("khoa-hoc-n5-co-ban")
                .shortDescription("Mô tả ngắn")
                .description("Mô tả chi tiết")
                .courseType(CourseType.FREE)
                .status(CourseStatus.PUBLISHED)
                .level(CourseLevel.N5)
                .originalPrice(BigDecimal.ZERO)
                .salePrice(BigDecimal.ZERO)
                .teacher(teacher)
                .totalDurationMinutes(120)
                .totalLessons(5)
                .build();

        when(courseRepository.findBySlugAndStatus("khoa-hoc-n5-co-ban", CourseStatus.PUBLISHED))
                .thenReturn(Optional.of(course));
        when(courseSectionRepository.findByCourseIdOrderBySortOrderAsc(10L))
                .thenReturn(Collections.emptyList());
        when(lessonRepository.findByCourseIdOrderBySortOrderAsc(10L))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void getCourseDetail_AsGuest_ReturnsIsEnrolledFalse() throws Exception {
        mockMvc.perform(get("/api/v1/courses/khoa-hoc-n5-co-ban")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(10))
                .andExpect(jsonPath("$.result.slug").value("khoa-hoc-n5-co-ban"))
                .andExpect(jsonPath("$.result.isEnrolled").value(false))
                .andExpect(jsonPath("$.result.lastLessonId").doesNotExist());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getCourseDetail_AsAuthenticatedEnrolledStudent_ReturnsIsEnrolledTrue() throws Exception {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(true);

        LessonProgressRepository.CourseLatestProgress latestProgress = new LessonProgressRepository.CourseLatestProgress() {
            @Override
            public Long getCourseId() {
                return 10L;
            }

            @Override
            public Long getLessonId() {
                return 42L;
            }

            @Override
            public String getLessonName() {
                return "Bài 3: Ngữ pháp desu";
            }

            @Override
            public String getLessonSlug() {
                return "bai-3-ngu-phap-desu";
            }
        };

        when(progressRepository.findLatestProgressForEachCourseByUserId(1L, List.of(10L)))
                .thenReturn(List.of(latestProgress));

        mockMvc.perform(get("/api/v1/courses/khoa-hoc-n5-co-ban")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.isEnrolled").value(true))
                .andExpect(jsonPath("$.result.lastLessonId").value(42));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getCourseDetail_AsAuthenticatedNotEnrolledStudent_ReturnsIsEnrolledFalse() throws Exception {
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 10L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/courses/khoa-hoc-n5-co-ban")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.isEnrolled").value(false));
    }
}
