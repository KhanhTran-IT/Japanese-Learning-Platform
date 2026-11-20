package com.japaneselearning.module_leaderboard.repository;

import com.japaneselearning.module_leaderboard.entity.LearningActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningActivityRepository extends JpaRepository<LearningActivity, Long> {
    
    // To check for idempotency on things like QUIZ_PASSED, LESSON_COMPLETED
    Optional<LearningActivity> findByUserIdAndActivityTypeAndReferenceId(Long userId, String activityType, Long referenceId);

    boolean existsByUserIdAndActivityTypeAndReferenceId(Long userId, String activityType, Long referenceId);
    
    // Paginated activity history with deterministic ordering per user
    Page<LearningActivity> findByUserId(Long userId, Pageable pageable);

    // Backward-compatible unpaginated query (deprecated for production UI consumption)
    List<LearningActivity> findByUserId(Long userId);

    // Aggregate total XP directly in database engine without loading complete history into memory
    @Query("SELECT COALESCE(SUM(la.xpAmount), 0) FROM LearningActivity la WHERE la.user.id = :userId")
    Long sumXpByUserId(@Param("userId") Long userId);

    // Retention cleanup: delete historical events older than cutoff date
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LearningActivity la WHERE la.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);

    // Count activities eligible for retention cleanup
    @Query("SELECT COUNT(la) FROM LearningActivity la WHERE la.createdAt < :cutoff")
    long countByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
