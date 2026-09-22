package com.japaneselearning.module_quiz.repository;

import com.japaneselearning.module_quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);

    List<QuizAttempt> findByUserId(Long userId);

    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    long countByUserIdAndQuizId(Long userId, Long quizId);

    @Query("SELECT COALESCE(MAX(a.attemptNumber), 0) FROM QuizAttempt a WHERE a.user.id = :userId AND a.quiz.id = :quizId")
    Integer findMaxAttemptNumberByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId);

    Optional<QuizAttempt> findFirstByUserIdAndQuizIdOrderByStartedAtDesc(Long userId, Long quizId);

    boolean existsByQuizId(Long quizId);

    @Query("SELECT qa FROM QuizAttempt qa JOIN FETCH qa.quiz WHERE qa.status = :status AND qa.quiz.timeLimitMinutes IS NOT NULL")
    List<QuizAttempt> findByStatusAndQuizTimeLimitMinutesIsNotNull(@Param("status") com.japaneselearning.module_quiz.enums.QuizAttemptStatus status);
}
