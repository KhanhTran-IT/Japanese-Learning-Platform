package com.japaneselearning.module_course.repository;

import com.japaneselearning.module_course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderBySortOrderAsc(Long courseId);
    List<Lesson> findBySectionIdOrderBySortOrderAsc(Long sectionId);
    Optional<Lesson> findByCourseIdAndSlug(Long courseId, String slug);
    boolean existsByCourseIdAndSlug(Long courseId, String slug);
    long countByCourseId(Long courseId);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Lesson l " +
           "WHERE l.course.id = :courseId " +
           "AND l.status = com.japaneselearning.module_course.enums.CourseStatus.PUBLISHED " +
           "AND l.section.status = com.japaneselearning.module_course.enums.CourseStatus.PUBLISHED")
    boolean hasPublishedLessonsInPublishedSections(@org.springframework.data.repository.query.Param("courseId") Long courseId);

    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId " +
           "AND l.status = :lessonStatus " +
           "AND l.section.status = :sectionStatus " +
           "ORDER BY l.sortOrder ASC")
    List<Lesson> findByCourseIdAndStatusAndSectionStatusOrderBySortOrderAsc(
            @org.springframework.data.repository.query.Param("courseId") Long courseId,
            @org.springframework.data.repository.query.Param("lessonStatus") com.japaneselearning.module_course.enums.CourseStatus lessonStatus,
            @org.springframework.data.repository.query.Param("sectionStatus") com.japaneselearning.module_course.enums.CourseStatus sectionStatus);

    @EntityGraph(attributePaths = {"section.course.teacher"})
    Optional<Lesson> findById(Long id);

    @EntityGraph(attributePaths = {"section.course.teacher"})
    @Query("SELECT l FROM Lesson l WHERE l.id = :id " +
           "AND l.status = com.japaneselearning.module_course.enums.CourseStatus.PUBLISHED " +
           "AND l.section.status = com.japaneselearning.module_course.enums.CourseStatus.PUBLISHED " +
           "AND l.course.status = com.japaneselearning.module_course.enums.CourseStatus.PUBLISHED")
    Optional<Lesson> findPublishedById(@org.springframework.data.repository.query.Param("id") Long id);

    @Query("SELECT MAX(l.sortOrder) FROM Lesson l WHERE l.section.id = :sectionId")
    Optional<Integer> findMaxSortOrderBySectionId(@org.springframework.data.repository.query.Param("sectionId") Long sectionId);

    interface CourseTotals {
        Long getTotalLessons();
        Long getTotalDurationMinutes();
    }

    @Query("SELECT COUNT(l) as totalLessons, COALESCE(SUM(l.durationMinutes), 0) as totalDurationMinutes " +
           "FROM Lesson l WHERE l.course.id = :courseId")
    CourseTotals getCourseTotals(@org.springframework.data.repository.query.Param("courseId") Long courseId);
}
