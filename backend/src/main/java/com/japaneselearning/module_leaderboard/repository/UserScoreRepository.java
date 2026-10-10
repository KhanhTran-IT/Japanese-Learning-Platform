package com.japaneselearning.module_leaderboard.repository;

import com.japaneselearning.module_leaderboard.entity.UserScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    
    Optional<UserScore> findByUserIdAndPeriodTypeAndPeriodValue(Long userId, String periodType, String periodValue);
    
    @Query("SELECT us FROM UserScore us JOIN FETCH us.user u WHERE us.periodType = :periodType AND us.periodValue = :periodValue AND u.isPrivateLeaderboard = false ORDER BY us.totalXp DESC")
    Page<UserScore> findTopByPeriod(@Param("periodType") String periodType, @Param("periodValue") String periodValue, Pageable pageable);

    @Query("SELECT COUNT(us) + 1 FROM UserScore us JOIN us.user u WHERE us.periodType = :periodType AND us.periodValue = :periodValue AND u.isPrivateLeaderboard = false AND us.totalXp > (SELECT us2.totalXp FROM UserScore us2 WHERE us2.userId = :userId AND us2.periodType = :periodType AND us2.periodValue = :periodValue)")
    Long getRankByUserIdAndPeriod(@Param("userId") Long userId, @Param("periodType") String periodType, @Param("periodValue") String periodValue);
}
