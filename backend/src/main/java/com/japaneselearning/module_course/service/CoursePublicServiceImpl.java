package com.japaneselearning.module_course.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.dto.publics.CourseDetailPublicRes;
import com.japaneselearning.module_course.dto.publics.CoursePublicRes;
import com.japaneselearning.module_course.dto.publics.LessonPublicRes;
import com.japaneselearning.module_course.dto.publics.SectionPublicRes;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursePublicServiceImpl implements CoursePublicService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CoursePublicRes> getPublishedCourses(String level, Pageable pageable) {
        Page<Course> courses;
        if (level != null && !level.trim().isEmpty()) {
            try {
                CourseLevel courseLevel = CourseLevel.valueOf(level.toUpperCase());
                courses = courseRepository.findByStatusAndLevel(CourseStatus.PUBLISHED, courseLevel, pageable);
            } catch (IllegalArgumentException e) {
                // If invalid level is passed, return empty or fallback to all published
                courses = courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable);
            }
        } else {
            courses = courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable);
        }

        return courses.map(course -> CoursePublicRes.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .shortDescription(course.getShortDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .level(course.getLevel() != null ? course.getLevel().name() : null)
                .courseType(course.getCourseType() != null ? course.getCourseType().name() : null)
                .originalPrice(course.getOriginalPrice())
                .salePrice(course.getSalePrice())
                .averageRating(course.getAverageRating())
                .totalStudents(course.getTotalStudents())
                .teacherName(course.getTeacher().getFullName())
                .teacherAvatarUrl(course.getTeacher().getAvatarUrl())
                .totalDurationMinutes(course.getTotalDurationMinutes())
                .totalLessons(course.getTotalLessons())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailPublicRes getCourseDetailBySlug(String slug) {
        Course course = courseRepository.findBySlugAndStatus(slug, CourseStatus.PUBLISHED)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        List<SectionPublicRes> sectionResList = course.getSections().stream()
                .sorted(Comparator.comparing(s -> s.getSortOrder() != null ? s.getSortOrder() : 0))
                .map(section -> {
                    List<LessonPublicRes> lessonResList = section.getLessons().stream()
                            .sorted(Comparator.comparing(l -> l.getSortOrder() != null ? l.getSortOrder() : 0))
                            .map(lesson -> {
                                LessonPublicRes lessonRes = LessonPublicRes.builder()
                                        .id(lesson.getId())
                                        .title(lesson.getTitle())
                                        .slug(lesson.getSlug())
                                        .isPreview(lesson.getIsPreview())
                                        .sortOrder(lesson.getSortOrder())
                                        .build();

                                // Data Protection: Hide content and videoUrl if not a preview lesson
                                if (Boolean.TRUE.equals(lesson.getIsPreview())) {
                                    lessonRes.setContent(lesson.getContent());
                                    lessonRes.setVideoUrl(lesson.getVideoUrl());
                                } else {
                                    lessonRes.setContent(null);
                                    lessonRes.setVideoUrl(null);
                                }

                                return lessonRes;
                            })
                            .toList();

                    return SectionPublicRes.builder()
                            .id(section.getId())
                            .title(section.getTitle())
                            .description(section.getDescription())
                            .sortOrder(section.getSortOrder())
                            .lessons(lessonResList)
                            .build();
                })
                .toList();

        return CourseDetailPublicRes.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .shortDescription(course.getShortDescription())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .level(course.getLevel() != null ? course.getLevel().name() : null)
                .courseType(course.getCourseType() != null ? course.getCourseType().name() : null)
                .originalPrice(course.getOriginalPrice())
                .salePrice(course.getSalePrice())
                .averageRating(course.getAverageRating())
                .totalStudents(course.getTotalStudents())
                .teacherName(course.getTeacher().getFullName())
                .teacherAvatarUrl(course.getTeacher().getAvatarUrl())
                .totalDurationMinutes(course.getTotalDurationMinutes())
                .totalLessons(course.getTotalLessons())
                .sections(sectionResList)
                .build();
    }
}
