package com.japaneselearning.module_course.service;

import com.japaneselearning.module_course.dto.SectionCreateReq;
import com.japaneselearning.module_course.dto.SectionRes;
import com.japaneselearning.module_course.dto.SectionUpdateReq;

import java.util.List;

public interface SectionAdminService {
    SectionRes createSection(Long courseId, SectionCreateReq req);
    List<SectionRes> getSectionsByCourseId(Long courseId);
    SectionRes updateSection(Long id, SectionUpdateReq req);
    void deleteSection(Long id);
}
