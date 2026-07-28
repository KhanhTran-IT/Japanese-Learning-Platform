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

    @EntityGraph(attributePaths = {"teacher", "sections", "sections.lessons"})
    Optional<Course> findBySlugAndStatus(String slug, CourseStatus status);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByStatusAndLevel(CourseStatus status, CourseLevel level, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    @Query("SELECT c FROM Course c WHERE c.status = 'PUBLISHED' " +
           "AND (:level IS NULL OR c.level = :level) " +
           "AND (:courseType IS NULL OR c.courseType = :courseType) " +
           "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchPublishedCourses(@Param("level") CourseLevel level,
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
