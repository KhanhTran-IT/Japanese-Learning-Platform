package com.japaneselearning.module_learning.service;

import com.japaneselearning.module_learning.dto.LessonLearningRes;
import com.japaneselearning.module_learning.dto.ProgressUpdateReq;

public interface LearningService {
    LessonLearningRes getLessonDetail(Long lessonId);
    void updateProgress(Long lessonId, ProgressUpdateReq req);
}
