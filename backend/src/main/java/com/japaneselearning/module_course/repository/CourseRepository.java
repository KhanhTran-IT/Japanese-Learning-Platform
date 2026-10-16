package com.japaneselearning.module_course.repository;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.japaneselearning.module_course.enums.CourseType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Modifying
    @Query("UPDATE Course c SET c.totalStudents = c.totalStudents + 1 WHERE c.id = :courseId")
    void incrementTotalStudents(@Param("courseId") Long courseId);

    @EntityGraph(attributePaths = {"teacher"})
    Optional<Course> findBySlugAndStatus(String slug, CourseStatus status);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByStatusAndLevel(CourseStatus status, CourseLevel level, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    @Query(value = "SELECT DISTINCT c FROM Course c WHERE c.status = 'PUBLISHED' " +
           "AND (:levelsEmpty = true OR c.level IN :levels) " +
           "AND (:courseType IS NULL OR c.courseType = :courseType) " +
           "AND (:keyword IS NULL " +
           "OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR EXISTS (SELECT 1 FROM Lesson l WHERE l.course = c AND l.status = 'PUBLISHED' " +
           "AND (LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(l.content) LIKE LOWER(CONCAT('%', :keyword, '%')))))",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Course c WHERE c.status = 'PUBLISHED' " +
           "AND (:levelsEmpty = true OR c.level IN :levels) " +
           "AND (:courseType IS NULL OR c.courseType = :courseType) " +
           "AND (:keyword IS NULL " +
           "OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR EXISTS (SELECT 1 FROM Lesson l WHERE l.course = c AND l.status = 'PUBLISHED' " +
           "AND (LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(l.content) LIKE LOWER(CONCAT('%', :keyword, '%')))))")
    Page<Course> searchPublishedCourses(@Param("levels") List<CourseLevel> levels,
                                        @Param("levelsEmpty") boolean levelsEmpty,
                                        @Param("courseType") CourseType courseType,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Optional<Course> findById(Long id);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByTeacherEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findTop5ByOrderByCreatedAtDesc();
}
