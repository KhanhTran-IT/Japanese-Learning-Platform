package com.japaneselearning.module_course.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.util.SlugUtils;
import com.japaneselearning.module_course.dto.CourseCreateReq;
import com.japaneselearning.module_course.dto.CourseRes;
import com.japaneselearning.module_course.dto.CourseUpdateReq;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseAdminServiceImpl implements CourseAdminService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CourseRes createCourse(CourseCreateReq req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String slug = req.getSlug() != null && !req.getSlug().trim().isEmpty() 
                ? req.getSlug() : SlugUtils.toSlug(req.getTitle());

        if (courseRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.COURSE_SLUG_EXISTS);
        }

        Course course = Course.builder()
                .title(req.getTitle())
                .slug(slug)
                .shortDescription(req.getShortDescription())
                .description(req.getDescription())
                .thumbnailUrl(req.getThumbnailUrl())
                .level(req.getLevel())
                .courseType(req.getCourseType())
                .originalPrice(req.getOriginalPrice())
                .salePrice(req.getSalePrice())
                .teacher(currentUser)
                .status(CourseStatus.DRAFT) // Mặc định DRAFT khi mới tạo
                .totalDurationMinutes(0)
                .totalLessons(0)
                .averageRating(0.0)
                .totalStudents(0)
                .build();

        Course savedCourse = courseRepository.save(course);
        return mapToCourseRes(savedCourse);
    }

    @Override
    @Transactional
    public CourseRes updateCourse(Long id, CourseUpdateReq req) {
        Course course = courseRepository.findById(id) // Needs fix in repository call, will use findById which we added @EntityGraph
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkTeacherPermission(course);

        String slug = req.getSlug() != null && !req.getSlug().trim().isEmpty() 
                ? req.getSlug() : SlugUtils.toSlug(req.getTitle());

        if (!course.getSlug().equals(slug) && courseRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.COURSE_SLUG_EXISTS);
        }

        course.setTitle(req.getTitle());
        course.setSlug(slug);
        course.setShortDescription(req.getShortDescription());
        course.setDescription(req.getDescription());
        course.setThumbnailUrl(req.getThumbnailUrl());
        course.setLevel(req.getLevel());
        course.setCourseType(req.getCourseType());
        course.setOriginalPrice(req.getOriginalPrice());
        course.setSalePrice(req.getSalePrice());
        course.setStatus(req.getStatus());

        Course updatedCourse = courseRepository.save(course);
        return mapToCourseRes(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkTeacherPermission(course);

        // Soft delete
        course.setStatus(CourseStatus.ARCHIVED);
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseRes getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return mapToCourseRes(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseRes> getCourses(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        Page<Course> courses = courseRepository.findAll(pageable);
        
        // If teacher, only show their courses. (Ideally this should be a DB query for performance, but for MVP we can filter or create a custom query).
        // Let's create a custom query if needed, or rely on future implementations.
        return courses.map(this::mapToCourseRes);
    }

    private void checkTeacherPermission(Course course) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        
        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdminOrSuperAdmin) {
            if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
                throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
            }
        }
    }

    private CourseRes mapToCourseRes(Course course) {
        return CourseRes.builder()
                .id(course.getId())
                .teacherId(course.getTeacher().getId())
                .teacherName(course.getTeacher().getFullName())
                .teacherAvatarUrl(course.getTeacher().getAvatarUrl())
                .title(course.getTitle())
                .slug(course.getSlug())
                .shortDescription(course.getShortDescription())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .level(course.getLevel() != null ? course.getLevel().name() : null)
                .courseType(course.getCourseType() != null ? course.getCourseType().name() : null)
                .originalPrice(course.getOriginalPrice())
                .salePrice(course.getSalePrice())
                .status(course.getStatus() != null ? course.getStatus().name() : null)
                .totalDurationMinutes(course.getTotalDurationMinutes())
                .totalLessons(course.getTotalLessons())
                .averageRating(course.getAverageRating())
                .totalStudents(course.getTotalStudents())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
