package com.japaneselearning.module_user.service;

import com.japaneselearning.module_learning.dto.MyCourseRes;
import com.japaneselearning.module_learning.dto.MyProgressOverviewRes;

import java.util.List;

public interface StudentDashboardService {
    List<MyCourseRes> getMyCourses();
    MyProgressOverviewRes getMyProgressOverview();
}
