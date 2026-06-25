package com.japaneselearning.module_course.repository;

import com.japaneselearning.module_course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"teacher", "sections", "sections.lessons"})
    Optional<Course> findBySlugAndStatus(String slug, com.japaneselearning.module_course.enums.CourseStatus status);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByStatus(com.japaneselearning.module_course.enums.CourseStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findByStatusAndLevel(com.japaneselearning.module_course.enums.CourseStatus status, com.japaneselearning.module_course.enums.CourseLevel level, Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Optional<Course> findById(Long id);
}
