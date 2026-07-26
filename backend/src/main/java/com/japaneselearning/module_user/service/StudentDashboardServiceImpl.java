package com.japaneselearning.module_user.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_learning.dto.MyCourseRes;
import com.japaneselearning.module_learning.dto.MyProgressOverviewRes;
import com.japaneselearning.module_learning.repository.LessonProgressRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentDashboardServiceImpl implements StudentDashboardService {

    private final UserRepository userRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository progressRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyCourseRes> getMyCourses() {
        User user = getCurrentUser();
        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(user.getId());

        if (enrollments.isEmpty()) {
            return List.of();
        }

        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());

        // Fetch completed counts per course in one query
        List<LessonProgressRepository.CourseProgressCount> counts = progressRepository.countCompletedLessonsByCourseForUser(user.getId(), courseIds);
        Map<Long, Integer> completedCountMap = counts.stream()
                .collect(Collectors.toMap(
                        LessonProgressRepository.CourseProgressCount::getCourseId,
                        c -> c.getCompletedCount() != null ? c.getCompletedCount().intValue() : 0
                ));

        // Fetch latest progress per course in one query
        List<LessonProgressRepository.CourseLatestProgress> latests = progressRepository.findLatestProgressForEachCourseByUserId(user.getId(), courseIds);
        Map<Long, LessonProgressRepository.CourseLatestProgress> latestProgressMap = latests.stream()
                .collect(Collectors.toMap(LessonProgressRepository.CourseLatestProgress::getCourseId, p -> p, (p1, p2) -> p1)); // in case of duplicates

        return enrollments.stream().map(enrollment -> {
            Course course = enrollment.getCourse();
            
            int totalLessons = course.getTotalLessons() != null ? course.getTotalLessons() : 0;
            int completedLessons = completedCountMap.getOrDefault(course.getId(), 0);
            
            double progressPercent = 0.0;
            if (totalLessons > 0) {
                progressPercent = ((double) completedLessons / totalLessons) * 100;
                // Round to 1 decimal place
                progressPercent = Math.round(progressPercent * 10.0) / 10.0;
            }

            LessonProgressRepository.CourseLatestProgress lastProgress = latestProgressMap.get(course.getId());

            String lastLessonName = lastProgress != null ? lastProgress.getLessonName() : null;
            String lastLessonSlug = lastProgress != null ? lastProgress.getLessonSlug() : null;

            return MyCourseRes.builder()
                    .courseId(course.getId())
                    .courseName(course.getTitle())
                    .slug(course.getSlug())
                    .thumbnailUrl(course.getThumbnailUrl())
                    .progressPercent(progressPercent)
                    .completedLessons(completedLessons)
                    .totalLessons(totalLessons)
                    .lastLessonName(lastLessonName)
                    .lastLessonSlug(lastLessonSlug)
                    .enrolledAt(enrollment.getEnrolledAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MyProgressOverviewRes getMyProgressOverview() {
        User user = getCurrentUser();
        
        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(user.getId());
        long totalEnrolledCourses = enrollments.size();
        
        if (enrollments.isEmpty()) {
            return MyProgressOverviewRes.builder()
                    .totalEnrolledCourses(0L)
                    .totalCompletedLessons(0L)
                    .overallProgressPercent(0.0)
                    .build();
        }
        
        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());
        int totalCompletedLessons = progressRepository.countByUserIdAndLessonCourseIdInAndIsCompletedTrue(user.getId(), courseIds);
        
        double overallProgressPercent = 0.0;
        
        int globalTotalLessons = enrollments.stream()
                .mapToInt(e -> e.getCourse().getTotalLessons() != null ? e.getCourse().getTotalLessons() : 0)
                .sum();
                
        if (globalTotalLessons > 0) {
            overallProgressPercent = ((double) totalCompletedLessons / globalTotalLessons) * 100;
            overallProgressPercent = Math.round(overallProgressPercent * 10.0) / 10.0;
        }

        return MyProgressOverviewRes.builder()
                .totalEnrolledCourses(totalEnrolledCourses)
                .totalCompletedLessons((long) totalCompletedLessons)
                .overallProgressPercent(overallProgressPercent)
                .build();
    }
}
