package com.japaneselearning.module_learning.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_learning.dto.LessonLearningRes;
import com.japaneselearning.module_learning.dto.ProgressUpdateReq;
import com.japaneselearning.module_learning.entity.LessonProgress;
import com.japaneselearning.module_learning.repository.LessonProgressRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final LessonRepository lessonRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository progressRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public LessonLearningRes getLessonDetail(Long lessonId) {
        LessonAccessContext context = validateAndGetLessonAccess(lessonId);

        // Fetch progress
        LessonProgress progress = progressRepository.findByUserIdAndLessonId(context.user().getId(), lessonId)
                .orElse(null);

        return LessonLearningRes.builder()
                .id(context.lesson().getId())
                .title(context.lesson().getTitle())
                .slug(context.lesson().getSlug())
                .content(context.lesson().getContent())
                .videoUrl(context.lesson().getVideoUrl())
                .isPreview(context.lesson().getIsPreview())
                .sortOrder(context.lesson().getSortOrder())
                .durationMinutes(context.lesson().getDurationMinutes())
                .watchedPercent(progress != null ? progress.getWatchedPercent() : 0.0)
                .isCompleted(progress != null ? progress.getIsCompleted() : false)
                .build();
    }

    @Override
    @Transactional
    public void updateProgress(Long lessonId, ProgressUpdateReq req) {
        LessonAccessContext context = validateAndGetLessonAccess(lessonId);

        Double newWatchedPercent = req.getWatchedPercent() != null ? req.getWatchedPercent() : 0.0;
        Boolean newIsCompleted = Boolean.TRUE.equals(req.getIsCompleted());
        LocalDateTime newCompletedAt = newIsCompleted ? LocalDateTime.now() : null;

        upsertLessonProgress(context.user(), context.lesson(), newWatchedPercent, newIsCompleted, newCompletedAt);
        recalculateEnrollmentProgress(context.user(), context.lesson());
    }

    @Override
    @Transactional
    public void completeLesson(Long lessonId) {
        LessonAccessContext context = validateAndGetLessonAccess(lessonId);

        upsertLessonProgress(context.user(), context.lesson(), 100.0, true, LocalDateTime.now());
        recalculateEnrollmentProgress(context.user(), context.lesson());
    }

    // --- Private Helpers ---

    private record LessonAccessContext(User user, Lesson lesson) {}

    private LessonAccessContext validateAndGetLessonAccess(Long lessonId) {
        User user = getCurrentUser();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Course course = lesson.getCourse();

        // Validate course status
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND); 
        }

        // Validate enrollment if not a preview lesson
        if (Boolean.FALSE.equals(lesson.getIsPreview())) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId());
            if (!isEnrolled) {
                throw new AppException(ErrorCode.FORBIDDEN_ACCESS);
            }
        }

        return new LessonAccessContext(user, lesson);
    }

    private void upsertLessonProgress(User user, Lesson lesson, Double watchedPercent, Boolean isCompleted, LocalDateTime completedAt) {
        int updated = progressRepository.updateProgressAtomically(user.getId(), lesson.getId(), watchedPercent, isCompleted, completedAt);
        
        if (updated == 0) {
            try {
                LessonProgress progress = LessonProgress.builder()
                        .user(user)
                        .lesson(lesson)
                        .watchedPercent(watchedPercent)
                        .isCompleted(isCompleted)
                        .completedAt(completedAt)
                        .build();
                progressRepository.save(progress);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Race condition on insert
                progressRepository.updateProgressAtomically(user.getId(), lesson.getId(), watchedPercent, isCompleted, completedAt);
            }
        }
    }

    private void recalculateEnrollmentProgress(User user, Lesson lesson) {
        Long courseId = lesson.getCourse().getId();
        int completedLessons = progressRepository.countByUserIdAndLessonCourseIdAndIsCompletedTrue(user.getId(), courseId);
        
        Integer totalLessonsObj = lesson.getCourse().getTotalLessons();
        long totalLessons = (totalLessonsObj != null) ? totalLessonsObj.longValue() : 0L;
        
        if (totalLessons == 0L) {
            LessonRepository.CourseTotals totals = lessonRepository.getCourseTotals(courseId);
            totalLessons = (totals != null && totals.getTotalLessons() != null) ? totals.getTotalLessons() : 0L;
        }

        int percent = 0;
        if (totalLessons > 0) {
            percent = (int) ((completedLessons * 100) / totalLessons);
            percent = Math.min(percent, 100);
        }

        enrollmentRepository.updateProgressPercent(user.getId(), courseId, percent);
    }
}
