package com.japaneselearning.module_course.service;

import com.japaneselearning.module_course.dto.publics.CourseDetailPublicRes;
import com.japaneselearning.module_course.dto.publics.CoursePublicRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CoursePublicService {
    Page<CoursePublicRes> getPublishedCourses(String level, Pageable pageable);
    CourseDetailPublicRes getCourseDetailBySlug(String slug);
}
