package com.japaneselearning.module_quiz.repository;

import com.japaneselearning.module_quiz.entity.Quiz;
import com.japaneselearning.module_quiz.enums.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCourseIdAndStatus(Long courseId, QuizStatus status);

    List<Quiz> findByLessonIdAndStatus(Long lessonId, QuizStatus status);

    List<Quiz> findByCourseId(Long courseId);

    List<Quiz> findByLessonId(Long lessonId);
}
