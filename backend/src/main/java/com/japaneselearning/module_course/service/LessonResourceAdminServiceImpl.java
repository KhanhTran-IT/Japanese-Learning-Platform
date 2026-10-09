package com.japaneselearning.module_course.service;

import com.japaneselearning.common.exception.AppException;
import com.japaneselearning.common.exception.ErrorCode;
import com.japaneselearning.module_course.dto.ResourceCreateReq;
import com.japaneselearning.module_course.dto.ResourceRes;
import com.japaneselearning.module_course.dto.ResourceUpdateReq;
import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.entity.LessonResource;
import com.japaneselearning.module_course.repository.LessonRepository;
import com.japaneselearning.module_course.repository.LessonResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonResourceAdminServiceImpl implements LessonResourceAdminService {

    private final LessonResourceRepository resourceRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public ResourceRes createResource(Long lessonId, ResourceCreateReq req) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        checkDataIsolation(lesson.getCourse());

        int sortOrder = req.getSortOrder() != null ? req.getSortOrder() : 0;

        LessonResource resource = LessonResource.builder()
                .lesson(lesson)
                .title(req.getTitle())
                .resourceType(req.getResourceType())
                .fileUrl(req.getFileUrl())
                .fileSize(req.getFileSize())
                .sortOrder(sortOrder)
                .build();

        LessonResource savedResource = resourceRepository.save(resource);
        return mapToResourceRes(savedResource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceRes> getResourcesByLessonId(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        checkDataIsolation(lesson.getCourse());

        return resourceRepository.findByLessonIdOrderBySortOrderAsc(lessonId)
                .stream()
                .map(this::mapToResourceRes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceRes getResource(Long id) {
        LessonResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        checkDataIsolation(resource.getLesson().getCourse());

        return mapToResourceRes(resource);
    }

    @Override
    @Transactional
    public ResourceRes updateResource(Long id, ResourceUpdateReq req) {
        LessonResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        checkDataIsolation(resource.getLesson().getCourse());

        resource.setTitle(req.getTitle());
        resource.setResourceType(req.getResourceType());
        resource.setFileUrl(req.getFileUrl());
        resource.setFileSize(req.getFileSize());
        resource.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : resource.getSortOrder());

        LessonResource updatedResource = resourceRepository.save(resource);
        return mapToResourceRes(updatedResource);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        LessonResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        checkDataIsolation(resource.getLesson().getCourse());

        resourceRepository.delete(resource);
    }

    private void checkDataIsolation(Course course) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();

        boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdminOrSuperAdmin) {
            // Role TEACHER must own this course
            if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
                throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
            }
        }
    }

    private ResourceRes mapToResourceRes(LessonResource resource) {
        return ResourceRes.builder()
                .id(resource.getId())
                .lessonId(resource.getLesson().getId())
                .title(resource.getTitle())
                .resourceType(resource.getResourceType() != null ? resource.getResourceType().name() : null)
                .fileUrl(resource.getFileUrl())
                .fileSize(resource.getFileSize())
                .sortOrder(resource.getSortOrder())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
