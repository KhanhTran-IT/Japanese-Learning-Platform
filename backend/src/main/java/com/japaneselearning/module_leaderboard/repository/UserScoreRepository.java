package com.japaneselearning.module_leaderboard.repository;

import com.japaneselearning.module_leaderboard.entity.UserScore;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    
    Optional<UserScore> findByUserIdAndPeriodTypeAndPeriodValue(Long userId, String periodType, String periodValue);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT us FROM UserScore us WHERE us.user.id = :userId AND us.periodType = :periodType AND us.periodValue = :periodValue")
    Optional<UserScore> findByUserIdAndPeriodTypeAndPeriodValueForUpdate(@Param("userId") Long userId, @Param("periodType") String periodType, @Param("periodValue") String periodValue);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserScore us SET us.totalXp = us.totalXp + :xpAmount, us.updatedAt = CURRENT_TIMESTAMP WHERE us.user.id = :userId AND us.periodType = :periodType AND us.periodValue = :periodValue")
    int incrementScore(@Param("userId") Long userId, @Param("periodType") String periodType, @Param("periodValue") String periodValue, @Param("xpAmount") Integer xpAmount);
    
    @Query(value = "SELECT us FROM UserScore us JOIN FETCH us.user u WHERE us.periodType = :periodType AND us.periodValue = :periodValue AND u.isPrivateLeaderboard = false ORDER BY us.totalXp DESC, us.id ASC",
           countQuery = "SELECT COUNT(us) FROM UserScore us JOIN us.user u WHERE us.periodType = :periodType AND us.periodValue = :periodValue AND u.isPrivateLeaderboard = false")
    Page<UserScore> findTopByPeriod(@Param("periodType") String periodType, @Param("periodValue") String periodValue, Pageable pageable);

    @Query("SELECT COUNT(us) + 1 FROM UserScore us JOIN us.user u WHERE us.periodType = :periodType AND us.periodValue = :periodValue AND u.isPrivateLeaderboard = false AND us.totalXp > (SELECT us2.totalXp FROM UserScore us2 WHERE us2.user.id = :userId AND us2.periodType = :periodType AND us2.periodValue = :periodValue)")
    Long getRankByUserIdAndPeriod(@Param("userId") Long userId, @Param("periodType") String periodType, @Param("periodValue") String periodValue);
}
