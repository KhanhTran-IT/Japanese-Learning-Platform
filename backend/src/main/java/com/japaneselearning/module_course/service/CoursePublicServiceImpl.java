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
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.entity.Lesson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.japaneselearning.module_user.repository.UserRepository;
import com.japaneselearning.module_enrollment.repository.CourseEnrollmentRepository;
import com.japaneselearning.module_learning.repository.LessonProgressRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoursePublicServiceImpl implements CoursePublicService {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository progressRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CoursePublicRes> getPublishedCourses(String keyword, String level, String courseType, Pageable pageable) {
        List<CourseLevel> parsedLevels = parseLevels(level);

        CourseType parsedCourseType = null;
        if (courseType != null && !courseType.trim().isEmpty()) {
            try {
                parsedCourseType = CourseType.valueOf(courseType.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Loại khóa học không hợp lệ: " + courseType);
            }
        }

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<CourseLevel> levelsForQuery = parsedLevels.isEmpty() ? List.of(CourseLevel.values()) : parsedLevels;

        Page<Course> courses = courseRepository.searchPublishedCourses(
                levelsForQuery,
                parsedLevels.isEmpty(),
                parsedCourseType,
                searchKeyword,
                pageable);

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

    private List<CourseLevel> parseLevels(String levels) {
        if (levels == null || levels.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return java.util.Arrays.stream(levels.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> {
                    try {
                        return CourseLevel.valueOf(value.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new AppException(ErrorCode.INVALID_REQUEST, "Cấp độ khóa học không hợp lệ: " + value);
                    }
                })
                .distinct()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailPublicRes getCourseDetailBySlug(String slug) {
        Course course = courseRepository.findBySlugAndStatus(slug, CourseStatus.PUBLISHED)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Use split queries to avoid Cartesian joins from nested collections
        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderBySortOrderAsc(course.getId());
        List<Lesson> allLessons = lessonRepository.findByCourseIdOrderBySortOrderAsc(course.getId());

        // Group lessons by section ID in memory
        Map<Long, List<Lesson>> lessonsBySectionId = allLessons.stream()
                .collect(Collectors.groupingBy(l -> l.getSection().getId()));

        List<SectionPublicRes> sectionResList = sections.stream()
                .map(section -> {
                    List<Lesson> sectionLessons = lessonsBySectionId.getOrDefault(section.getId(), Collections.emptyList());
                    List<LessonPublicRes> lessonResList = sectionLessons.stream()
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

        Boolean isEnrolled = false;
        Long lastLessonId = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName();
            Optional<com.japaneselearning.module_user.entity.User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                Long userId = userOpt.get().getId();
                if (enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId())) {
                    isEnrolled = true;
                    List<LessonProgressRepository.CourseLatestProgress> latestProgress =
                            progressRepository.findLatestProgressForEachCourseByUserId(userId, List.of(course.getId()));
                    if (!latestProgress.isEmpty() && latestProgress.get(0).getLessonId() != null) {
                        lastLessonId = latestProgress.get(0).getLessonId();
                    }
                }
            }
        }

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
                .isEnrolled(isEnrolled)
                .lastLessonId(lastLessonId)
                .sections(sectionResList)
                .build();
    }
}
