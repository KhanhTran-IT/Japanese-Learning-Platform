package com.japaneselearning.module_quiz.repository;

import com.japaneselearning.module_quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);

    List<QuizAttempt> findByUserId(Long userId);

    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    long countByUserIdAndQuizId(Long userId, Long quizId);
    
    boolean existsByQuizId(Long quizId);
}
