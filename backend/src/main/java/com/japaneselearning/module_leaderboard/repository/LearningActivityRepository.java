package com.japaneselearning.module_leaderboard.repository;

import com.japaneselearning.module_leaderboard.entity.LearningActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningActivityRepository extends JpaRepository<LearningActivity, Long> {
    
    // To check for idempotency on things like QUIZ_PASSED
    Optional<LearningActivity> findByUserIdAndActivityTypeAndReferenceId(Long userId, String activityType, Long referenceId);
    
    List<LearningActivity> findByUserId(Long userId);
}
