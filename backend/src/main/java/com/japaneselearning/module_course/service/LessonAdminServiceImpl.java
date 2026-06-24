package com.japaneselearning.module_course.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.util.SlugUtils;
import com.japaneselearning.module_course.dto.LessonCreateReq;
import com.japaneselearning.module_course.dto.LessonRes;
import com.japaneselearning.module_course.dto.LessonUpdateReq;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonAdminServiceImpl implements LessonAdminService {

    private final LessonRepository lessonRepository;
    private final CourseSectionRepository sectionRepository;

    @Override
    @Transactional
    public LessonRes createLesson(Long sectionId, LessonCreateReq req) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));

        checkDataIsolation(section.getCourse());

        String slug = req.getSlug() != null && !req.getSlug().trim().isEmpty()
                ? req.getSlug() : SlugUtils.toSlug(req.getTitle());

        if (lessonRepository.existsByCourseIdAndSlug(section.getCourse().getId(), slug)) {
            throw new AppException(ErrorCode.LESSON_SLUG_EXISTS);
        }

        int sortOrder = req.getSortOrder() != null ? req.getSortOrder() : getNextSortOrder(sectionId);

        Lesson lesson = Lesson.builder()
                .section(section)
                .course(section.getCourse())
                .title(req.getTitle())
                .slug(slug)
                .content(req.getContent())
                .videoUrl(req.getVideoUrl())
                .isPreview(req.getIsPreview() != null ? req.getIsPreview() : false)
                .sortOrder(sortOrder)
                .status(CourseStatus.DRAFT) // Mặc định khi tạo mới
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);
        return mapToLessonRes(savedLesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonRes> getLessonsBySectionId(Long sectionId) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));

        checkDataIsolation(section.getCourse());

        return lessonRepository.findBySectionIdOrderBySortOrderAsc(sectionId)
                .stream()
                .map(this::mapToLessonRes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LessonRes getLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        checkDataIsolation(lesson.getCourse());

        return mapToLessonRes(lesson);
    }

    @Override
    @Transactional
    public LessonRes updateLesson(Long id, LessonUpdateReq req) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        checkDataIsolation(lesson.getCourse());

        String slug = req.getSlug() != null && !req.getSlug().trim().isEmpty()
                ? req.getSlug() : SlugUtils.toSlug(req.getTitle());

        if (!lesson.getSlug().equals(slug) && lessonRepository.existsByCourseIdAndSlug(lesson.getCourse().getId(), slug)) {
            throw new AppException(ErrorCode.LESSON_SLUG_EXISTS);
        }

        lesson.setTitle(req.getTitle());
        lesson.setSlug(slug);
        lesson.setContent(req.getContent());
        lesson.setVideoUrl(req.getVideoUrl());
        lesson.setIsPreview(req.getIsPreview());
        lesson.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : lesson.getSortOrder());
        lesson.setStatus(req.getStatus());

        Lesson updatedLesson = lessonRepository.save(lesson);
        return mapToLessonRes(updatedLesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        checkDataIsolation(lesson.getCourse());

        lessonRepository.delete(lesson);
    }

    private void checkDataIsolation(Course course) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdminOrSuperAdmin) {
            // Role TEACHER phải sở hữu khóa học này
            if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
                throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
            }
        }
    }

    private int getNextSortOrder(Long sectionId) {
        return lessonRepository.findMaxSortOrderBySectionId(sectionId).orElse(0) + 1;
    }

    private LessonRes mapToLessonRes(Lesson lesson) {
        return LessonRes.builder()
                .id(lesson.getId())
                .sectionId(lesson.getSection().getId())
                .title(lesson.getTitle())
                .slug(lesson.getSlug())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .isPreview(lesson.getIsPreview())
                .sortOrder(lesson.getSortOrder())
                .status(lesson.getStatus() != null ? lesson.getStatus().name() : null)
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
