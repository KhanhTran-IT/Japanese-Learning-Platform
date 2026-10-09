package com.japaneselearning.module_course.service;

import com.japaneselearning.module_course.dto.CourseCreateReq;
import com.japaneselearning.module_course.dto.CourseRes;
import com.japaneselearning.module_course.dto.CourseUpdateReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseAdminService {
    CourseRes createCourse(CourseCreateReq req);
    CourseRes updateCourse(Long id, CourseUpdateReq req);
    void deleteCourse(Long id);
    CourseRes getCourse(Long id);
    Page<CourseRes> getCourses(Pageable pageable);
    CourseRes publishCourse(Long id);
    CourseRes hideCourse(Long id);
}
