package com.japaneselearning.module_enrollment.repository;

import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    @EntityGraph(attributePaths = {"course"})
    List<CourseEnrollment> findByUserId(Long userId);
}
