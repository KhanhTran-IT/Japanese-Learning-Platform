package com.japaneselearning.module_admin.service;

import com.japaneselearning.module_admin.dto.AdminDashboardRes;
import com.japaneselearning.module_admin.dto.RecentCourseRes;
import com.japaneselearning.module_admin.dto.RecentUserRes;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_user.entity.Role;
import com.japaneselearning.module_user.enums.RoleName;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    @Override
    public AdminDashboardRes getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long totalLessons = lessonRepository.count();
        long totalEnrollments = courseEnrollmentRepository.count();

        List<RecentUserRes> recentUsers = userRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(user -> {
                    String primaryRole = determinePrimaryRole(user.getRoles());
                    return RecentUserRes.builder()
                            .id(user.getId())
                            .fullName(user.getFullName())
                            .email(user.getEmail())
                            .role(primaryRole)
                            .createdAt(user.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        List<RecentCourseRes> recentCourses = courseRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(course -> RecentCourseRes.builder()
                        .id(course.getId())
                        .title(course.getTitle())
                        .teacherName(course.getTeacher() != null ? course.getTeacher().getFullName() : null)
                        .isPublished(course.getStatus() == CourseStatus.PUBLISHED)
                        .createdAt(course.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return AdminDashboardRes.builder()
                .totalUsers(totalUsers)
                .totalCourses(totalCourses)
                .totalLessons(totalLessons)
                .totalEnrollments(totalEnrollments)
                .recentUsers(recentUsers)
                .recentCourses(recentCourses)
                .build();
    }

    private String determinePrimaryRole(java.util.Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return RoleName.GUEST.name();
        }
        
        List<String> roleNames = roles.stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        if (roleNames.contains(RoleName.SUPER_ADMIN.name())) return RoleName.SUPER_ADMIN.name();
        if (roleNames.contains(RoleName.ADMIN.name())) return RoleName.ADMIN.name();
        if (roleNames.contains(RoleName.TEACHER.name())) return RoleName.TEACHER.name();
        if (roleNames.contains(RoleName.CONTENT_EDITOR.name())) return RoleName.CONTENT_EDITOR.name();
        if (roleNames.contains(RoleName.STUDENT.name())) return RoleName.STUDENT.name();
        
        return roleNames.get(0);
    }
}
