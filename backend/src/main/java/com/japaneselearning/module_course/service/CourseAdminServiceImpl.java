package com.japaneselearning.module_course.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.common.util.SlugUtils;
import com.japaneselearning.module_course.dto.CourseCreateReq;
import com.japaneselearning.module_course.dto.CourseRes;
import com.japaneselearning.module_course.dto.CourseUpdateReq;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CourseAdminServiceImpl implements CourseAdminService {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CourseRes createCourse(CourseCreateReq req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        validatePricing(req.getCourseType(), req.getOriginalPrice(), req.getSalePrice());

        String slug = req.getSlug() != null && !req.getSlug().trim().isEmpty() 
                ? req.getSlug() : SlugUtils.toSlug(req.getTitle());

        if (courseRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.COURSE_SLUG_EXISTS);
        }

        BigDecimal originalPrice = req.getCourseType() == CourseType.FREE 
                ? BigDecimal.ZERO : req.getOriginalPrice();
        BigDecimal salePrice = req.getCourseType() == CourseType.FREE 
                ? BigDecimal.ZERO : req.getSalePrice();

        Course course = Course.builder()
                .title(req.getTitle())
                .slug(slug)
                .shortDescription(req.getShortDescription())
                .description(req.getDescription())
                .thumbnailUrl(req.getThumbnailUrl())
                .level(req.getLevel())
                .courseType(req.getCourseType())
                .originalPrice(originalPrice)
                .salePrice(salePrice)
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
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkTeacherPermission(course);

        if (!course.getStatus().canTransitionTo(req.getStatus())) {
            throw new AppException(ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        validatePricing(req.getCourseType(), req.getOriginalPrice(), req.getSalePrice());

        // Prevent update requests from bypassing publication validation
        if (req.getStatus() == CourseStatus.PUBLISHED) {
            validatePublicationCurriculum(course.getId());
        }

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
        if (req.getCourseType() == CourseType.FREE) {
            course.setOriginalPrice(BigDecimal.ZERO);
            course.setSalePrice(BigDecimal.ZERO);
        } else {
            course.setOriginalPrice(req.getOriginalPrice());
            course.setSalePrice(req.getSalePrice());
        }
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

        if (!course.getStatus().canTransitionTo(CourseStatus.ARCHIVED)) {
            throw new AppException(ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        // Soft delete
        course.setStatus(CourseStatus.ARCHIVED);
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseRes getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        checkTeacherPermission(course);
        return mapToCourseRes(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseRes> getCourses(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        Page<Course> courses;
        if (isAdminOrSuperAdmin) {
            courses = courseRepository.findAll(pageable);
        } else {
            // Teacher only sees their own courses
            courses = courseRepository.findByTeacherEmail(auth.getName(), pageable);
        }

        return courses.map(this::mapToCourseRes);
    }

    @Override
    @Transactional
    public CourseRes publishCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkTeacherPermission(course);

        if (!course.getStatus().canTransitionTo(CourseStatus.PUBLISHED)) {
            throw new AppException(ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        validatePublicationRequirements(course, course.getCourseType(), course.getOriginalPrice(), course.getSalePrice());

        course.setStatus(CourseStatus.PUBLISHED);
        Course updatedCourse = courseRepository.save(course);
        return mapToCourseRes(updatedCourse);
    }

    @Override
    @Transactional
    public CourseRes hideCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkTeacherPermission(course);

        if (!course.getStatus().canTransitionTo(CourseStatus.HIDDEN)) {
            throw new AppException(ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }

        course.setStatus(CourseStatus.HIDDEN);
        Course updatedCourse = courseRepository.save(course);
        return mapToCourseRes(updatedCourse);
    }

    private void validatePricing(CourseType courseType, BigDecimal originalPrice, BigDecimal salePrice) {
        if (courseType == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (courseType == CourseType.FREE) {
            if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                throw new AppException(ErrorCode.COURSE_FREE_PRICE_INVALID);
            }
            if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0) {
                throw new AppException(ErrorCode.COURSE_FREE_PRICE_INVALID);
            }
            if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        } else if (courseType == CourseType.PAID) {
            if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.COURSE_PRICE_REQUIRED);
            }
            if (salePrice != null) {
                if (salePrice.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new AppException(ErrorCode.COURSE_SALE_PRICE_INVALID);
                }
                if (salePrice.compareTo(originalPrice) > 0) {
                    throw new AppException(ErrorCode.COURSE_SALE_PRICE_INVALID);
                }
            }
        }
    }

    private void validatePublicationRequirements(Course course, CourseType courseType, BigDecimal originalPrice, BigDecimal salePrice) {
        validatePricing(courseType, originalPrice, salePrice);
        validatePublicationCurriculum(course.getId());
    }

    private void validatePublicationCurriculum(Long courseId) {
        long sectionCount = sectionRepository.countByCourseId(courseId);
        long lessonCount = lessonRepository.countByCourseId(courseId);
        if (sectionCount == 0 || lessonCount == 0) {
            throw new AppException(ErrorCode.COURSE_CANNOT_PUBLISH_EMPTY);
        }

        boolean hasPublishedSection = sectionRepository.existsByCourseIdAndStatus(courseId, CourseStatus.PUBLISHED);
        if (!hasPublishedSection) {
            throw new AppException(ErrorCode.COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS);
        }

        boolean hasPublishedLessonInPublishedSection = lessonRepository.hasPublishedLessonsInPublishedSections(courseId);
        if (!hasPublishedLessonInPublishedSection) {
            throw new AppException(ErrorCode.COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS);
        }
    }

    private void checkTeacherPermission(Course course) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        
        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdminOrSuperAdmin) {
            if (course == null || course.getTeacher() == null || !course.getTeacher().getEmail().equals(currentUserEmail)) {
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
