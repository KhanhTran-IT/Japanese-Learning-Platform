package com.japaneselearning.module_course.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.dto.SectionCreateReq;
import com.japaneselearning.module_course.dto.SectionRes;
import com.japaneselearning.module_course.dto.SectionUpdateReq;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.repository.CourseRepository;
import com.japaneselearning.module_course.repository.CourseSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionAdminServiceImpl implements SectionAdminService {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;

    @Override
    @Transactional
    public SectionRes createSection(Long courseId, SectionCreateReq req) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkDataIsolation(course);

        int sortOrder = req.getSortOrder() != null ? req.getSortOrder() : getNextSortOrder(courseId);

        CourseSection section = CourseSection.builder()
                .course(course)
                .title(req.getTitle())
                .description(req.getDescription())
                .sortOrder(sortOrder)
                .status(CourseStatus.DRAFT) // Mặc định khi tạo mới
                .build();

        CourseSection savedSection = sectionRepository.save(section);
        return mapToSectionRes(savedSection);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionRes> getSectionsByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        checkDataIsolation(course);

        return sectionRepository.findByCourseIdOrderBySortOrderAsc(courseId)
                .stream()
                .map(this::mapToSectionRes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectionRes updateSection(Long id, SectionUpdateReq req) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));

        checkDataIsolation(section.getCourse());

        section.setTitle(req.getTitle());
        section.setDescription(req.getDescription());
        section.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : section.getSortOrder());
        section.setStatus(req.getStatus());

        CourseSection updatedSection = sectionRepository.save(section);
        return mapToSectionRes(updatedSection);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));

        checkDataIsolation(section.getCourse());

        if (section.getLessons() != null && !section.getLessons().isEmpty()) {
            throw new AppException(ErrorCode.SECTION_HAS_LESSONS);
        }

        sectionRepository.delete(section);
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

    private int getNextSortOrder(Long courseId) {
        return sectionRepository.findMaxSortOrderByCourseId(courseId).orElse(0) + 1;
    }

    private SectionRes mapToSectionRes(CourseSection section) {
        return SectionRes.builder()
                .id(section.getId())
                .courseId(section.getCourse().getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .sortOrder(section.getSortOrder())
                .status(section.getStatus() != null ? section.getStatus().name() : null)
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}
