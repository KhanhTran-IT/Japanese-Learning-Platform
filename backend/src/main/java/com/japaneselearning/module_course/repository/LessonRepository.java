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
}
