package com.japaneselearning.module_enrollment.repository;

import com.japaneselearning.module_enrollment.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    @EntityGraph(attributePaths = {"course"})
    List<CourseEnrollment> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE CourseEnrollment e SET e.progressPercent = :percent " +
           "WHERE e.user.id = :userId AND e.course.id = :courseId")
    int updateProgressPercent(@Param("userId") Long userId,
                              @Param("courseId") Long courseId,
                              @Param("percent") Integer percent);
}
