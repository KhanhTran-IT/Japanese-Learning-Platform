package com.japaneselearning.module_course.repository;

import com.japaneselearning.module_course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderBySortOrderAsc(Long courseId);
    List<Lesson> findBySectionIdOrderBySortOrderAsc(Long sectionId);
    Optional<Lesson> findByCourseIdAndSlug(Long courseId, String slug);
    boolean existsByCourseIdAndSlug(Long courseId, String slug);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"section.course.teacher"})
    Optional<Lesson> findById(Long id);

    @org.springframework.data.jpa.repository.Query("SELECT MAX(l.sortOrder) FROM Lesson l WHERE l.section.id = :sectionId")
    Optional<Integer> findMaxSortOrderBySectionId(@org.springframework.data.repository.query.Param("sectionId") Long sectionId);

    interface CourseTotals {
        Long getTotalLessons();
        Long getTotalDurationMinutes();
    }

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(l) as totalLessons, COALESCE(SUM(l.durationMinutes), 0) as totalDurationMinutes " +
           "FROM Lesson l WHERE l.course.id = :courseId")
    CourseTotals getCourseTotals(@org.springframework.data.repository.query.Param("courseId") Long courseId);
}
