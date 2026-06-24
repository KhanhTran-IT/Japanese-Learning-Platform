package com.japaneselearning.module_course.service;

import com.japaneselearning.module_course.dto.LessonCreateReq;
import com.japaneselearning.module_course.dto.LessonRes;
import com.japaneselearning.module_course.dto.LessonUpdateReq;

import java.util.List;

public interface LessonAdminService {
    LessonRes createLesson(Long sectionId, LessonCreateReq req);
    List<LessonRes> getLessonsBySectionId(Long sectionId);
    LessonRes getLesson(Long id);
    LessonRes updateLesson(Long id, LessonUpdateReq req);
    void deleteLesson(Long id);
}
