package com.japaneselearning.module_enrollment.repository;

import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
