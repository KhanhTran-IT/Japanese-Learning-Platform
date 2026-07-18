package com.japaneselearning.module_course.repository;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"teacher", "sections", "sections.lessons"})
    Optional<Course> findBySlugAndStatus(String slug, CourseStatus status);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByStatusAndLevel(CourseStatus status, CourseLevel level, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Optional<Course> findById(Long id);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByTeacherEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    List<Course> findTop5ByOrderByCreatedAtDesc();
}
