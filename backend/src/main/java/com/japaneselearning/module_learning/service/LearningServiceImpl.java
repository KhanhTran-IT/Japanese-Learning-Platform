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
        User user = getCurrentUser();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Course course = lesson.getCourse();

        // Validate course status
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND); // Or a specific error
        }

        // Validate enrollment if not a preview lesson
        if (Boolean.FALSE.equals(lesson.getIsPreview())) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId());
            if (!isEnrolled) {
                throw new AppException(ErrorCode.FORBIDDEN_ACCESS);
            }
        }

        // Fetch progress
        LessonProgress progress = progressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .orElse(null);

        return LessonLearningRes.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .slug(lesson.getSlug())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .isPreview(lesson.getIsPreview())
                .sortOrder(lesson.getSortOrder())
                .durationMinutes(lesson.getDurationMinutes())
                .watchedPercent(progress != null ? progress.getWatchedPercent() : 0.0)
                .isCompleted(progress != null ? progress.getIsCompleted() : false)
                .build();
    }

    @Override
    @Transactional
    public void updateProgress(Long lessonId, ProgressUpdateReq req) {
        User user = getCurrentUser();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        // Similar access validation as GET, to prevent forging POST requests for unauthorized lessons
        if (Boolean.FALSE.equals(lesson.getIsPreview())) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(user.getId(), lesson.getCourse().getId());
            if (!isEnrolled) {
                throw new AppException(ErrorCode.FORBIDDEN_ACCESS);
            }
        }

        // Upsert progress
        LessonProgress progress = progressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .orElse(LessonProgress.builder()
                        .user(user)
                        .lesson(lesson)
                        .watchedPercent(0.0)
                        .isCompleted(false)
                        .build());

        // Only update watchedPercent if the new value is greater
        if (req.getWatchedPercent() != null && req.getWatchedPercent() > progress.getWatchedPercent()) {
            progress.setWatchedPercent(req.getWatchedPercent());
        }

        // Mark as completed
        if (Boolean.TRUE.equals(req.getIsCompleted()) && !Boolean.TRUE.equals(progress.getIsCompleted())) {
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        progressRepository.save(progress);
    }
}
