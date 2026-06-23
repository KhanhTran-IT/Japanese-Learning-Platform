package com.japaneselearning.module_course.repository;

import com.japaneselearning.module_course.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    List<CourseSection> findByCourseIdOrderBySortOrderAsc(Long courseId);

    @Query("SELECT MAX(s.sortOrder) FROM CourseSection s WHERE s.course.id = :courseId")
    Optional<Integer> findMaxSortOrderByCourseId(@Param("courseId") Long courseId);
}
