package com.japaneselearning.module_learning.repository;

import com.japaneselearning.module_learning.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    int countByUserIdAndIsCompletedTrue(Long userId);

    int countByUserIdAndLessonCourseIdAndIsCompletedTrue(Long userId, Long courseId);

    Optional<LessonProgress> findFirstByUserIdAndLessonCourseIdOrderByUpdatedAtDesc(Long userId, Long courseId);

    @Modifying
    @Query("UPDATE LessonProgress lp " +
           "SET lp.watchedPercent = CASE WHEN :watchedPercent > lp.watchedPercent THEN :watchedPercent ELSE lp.watchedPercent END, " +
           "    lp.isCompleted = CASE WHEN :isCompleted = true THEN true ELSE lp.isCompleted END, " +
           "    lp.completedAt = CASE WHEN :isCompleted = true AND lp.isCompleted = false THEN :completedAt ELSE lp.completedAt END " +
           "WHERE lp.user.id = :userId AND lp.lesson.id = :lessonId")
    int updateProgressAtomically(@Param("userId") Long userId, 
                                 @Param("lessonId") Long lessonId, 
                                 @Param("watchedPercent") Double watchedPercent, 
                                 @Param("isCompleted") Boolean isCompleted, 
                                 @Param("completedAt") LocalDateTime completedAt);
}
