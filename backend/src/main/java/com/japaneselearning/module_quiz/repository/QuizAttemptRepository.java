package com.japaneselearning.module_quiz.repository;

import com.japaneselearning.module_quiz.entity.QuizAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    /**
     * Count attempts with a pessimistic write lock to prevent concurrent
     * threads from reading a stale count during the check-then-insert
     * in startAttempt(). The lock is held until the enclosing transaction commits.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(a) FROM QuizAttempt a WHERE a.user.id = :userId AND a.quiz.id = :quizId")
    long countByUserIdAndQuizIdForUpdate(@Param("userId") Long userId, @Param("quizId") Long quizId);

    Optional<QuizAttempt> findFirstByUserIdAndQuizIdOrderByStartedAtDesc(Long userId, Long quizId);

    boolean existsByQuizId(Long quizId);
}

