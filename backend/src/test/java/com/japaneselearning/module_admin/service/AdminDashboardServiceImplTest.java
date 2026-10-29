package com.japaneselearning.module_admin.service;

import com.japaneselearning.module_admin.dto.AdminDashboardRes;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    @Test
    @DisplayName("getDashboardStats should have @Transactional(readOnly = true)")
    void getDashboardStats_shouldHaveTransactionalReadOnlyAnnotation() throws NoSuchMethodException {
        Method method = AdminDashboardServiceImpl.class.getMethod("getDashboardStats");
        Transactional annotation = method.getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }

    @Test
    @DisplayName("getDashboardStats should aggregate counts and recent entries accurately")
    void getDashboardStats_shouldAggregateSuccessfully() {
        when(userRepository.count()).thenReturn(150L);
        when(courseRepository.count()).thenReturn(12L);
        when(lessonRepository.count()).thenReturn(85L);
        when(courseEnrollmentRepository.count()).thenReturn(340L);

        Role studentRole = Role.builder().name(RoleName.STUDENT).build();
        User testUser = User.builder()
                .id(1L)
                .fullName("Tanaka Taro")
                .email("tanaka@example.com")
                .roles(Set.of(studentRole))
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(testUser));

        User teacher = User.builder()
                .id(2L)
                .fullName("Sensei Honda")
                .build();
        Course testCourse = Course.builder()
                .id(10L)
                .title("JLPT N3 Mastery")
                .teacher(teacher)
                .status(CourseStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .build();
        when(courseRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(testCourse));

        AdminDashboardRes result = adminDashboardService.getDashboardStats();

        assertThat(result.getTotalUsers()).isEqualTo(150L);
        assertThat(result.getTotalCourses()).isEqualTo(12L);
        assertThat(result.getTotalLessons()).isEqualTo(85L);
        assertThat(result.getTotalEnrollments()).isEqualTo(340L);

        assertThat(result.getRecentUsers()).hasSize(1);
        assertThat(result.getRecentUsers().get(0).getFullName()).isEqualTo("Tanaka Taro");
        assertThat(result.getRecentUsers().get(0).getRole()).isEqualTo("STUDENT");

        assertThat(result.getRecentCourses()).hasSize(1);
        assertThat(result.getRecentCourses().get(0).getTitle()).isEqualTo("JLPT N3 Mastery");
        assertThat(result.getRecentCourses().get(0).getTeacherName()).isEqualTo("Sensei Honda");
        assertThat(result.getRecentCourses().get(0).getIsPublished()).isTrue();
    }
}
