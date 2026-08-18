package com.japaneselearning.module_course.repository;

import com.japaneselearning.module_course.entity.LessonResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonResourceRepository extends JpaRepository<LessonResource, Long> {
    List<LessonResource> findByLessonIdOrderBySortOrderAsc(Long lessonId);
}
