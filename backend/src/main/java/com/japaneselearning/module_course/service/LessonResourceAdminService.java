package com.japaneselearning.module_course.service;

import com.japaneselearning.module_course.dto.ResourceCreateReq;
import com.japaneselearning.module_course.dto.ResourceRes;
import com.japaneselearning.module_course.dto.ResourceUpdateReq;

import java.util.List;

public interface LessonResourceAdminService {
    ResourceRes createResource(Long lessonId, ResourceCreateReq req);
    List<ResourceRes> getResourcesByLessonId(Long lessonId);
    ResourceRes getResource(Long id);
    ResourceRes updateResource(Long id, ResourceUpdateReq req);
    void deleteResource(Long id);
}
