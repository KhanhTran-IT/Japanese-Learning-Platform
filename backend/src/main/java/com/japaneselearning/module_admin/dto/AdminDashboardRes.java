package com.japaneselearning.module_admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardRes {
    private long totalUsers;
    private long totalCourses;
    private long totalLessons;
    private long totalEnrollments;
    private List<RecentUserRes> recentUsers;
    private List<RecentCourseRes> recentCourses;
}
