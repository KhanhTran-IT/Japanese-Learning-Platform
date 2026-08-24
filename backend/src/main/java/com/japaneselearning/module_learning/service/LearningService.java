package com.japaneselearning.module_learning.service;

import com.japaneselearning.module_learning.dto.LessonLearningRes;
import com.japaneselearning.module_learning.dto.ProgressUpdateReq;
import com.japaneselearning.module_course.dto.ResourceRes;
import com.japaneselearning.module_learning.dto.LearningCurriculumRes;

import java.util.List;

public interface LearningService {
    LessonLearningRes getLessonDetail(Long lessonId);
    void updateProgress(Long lessonId, ProgressUpdateReq req);
    void completeLesson(Long lessonId);
    List<ResourceRes> getLessonResources(Long lessonId);
    LearningCurriculumRes getLessonCurriculum(Long lessonId);
}
