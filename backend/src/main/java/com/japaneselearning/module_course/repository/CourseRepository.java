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

    @EntityGraph(attributePaths = {"teacher"})
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"teacher"})
    Optional<Course> findById(Long id);
}
