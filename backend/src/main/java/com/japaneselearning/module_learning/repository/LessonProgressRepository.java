package com.japaneselearning.module_learning.repository;

import com.japaneselearning.module_learning.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    List<LessonProgress> findByUserIdAndLessonCourseId(Long userId, Long courseId);

    int countByUserIdAndIsCompletedTrue(Long userId);

    int countByUserIdAndLessonCourseIdAndIsCompletedTrue(Long userId, Long courseId);

    int countByUserIdAndLessonCourseIdInAndIsCompletedTrue(Long userId, List<Long> courseIds);

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

    interface CourseProgressCount {
        Long getCourseId();
        Long getCompletedCount();
    }

    @Query("SELECT lp.lesson.course.id as courseId, COUNT(lp) as completedCount " +
           "FROM LessonProgress lp " +
           "WHERE lp.user.id = :userId AND lp.isCompleted = true " +
           "AND lp.lesson.course.id IN :courseIds " +
           "GROUP BY lp.lesson.course.id")
    List<CourseProgressCount> countCompletedLessonsByCourseForUser(@Param("userId") Long userId, @Param("courseIds") List<Long> courseIds);

    interface CourseLatestProgress {
        Long getCourseId();
        Long getLessonId();
        String getLessonName();
        String getLessonSlug();
    }

    @Query("SELECT l.course.id as courseId, l.id as lessonId, l.title as lessonName, l.slug as lessonSlug " +
           "FROM LessonProgress lp " +
           "JOIN lp.lesson l " +
           "WHERE lp.user.id = :userId AND l.course.id IN :courseIds AND lp.id = (" +
           "   SELECT MAX(lp2.id) FROM LessonProgress lp2 " +
           "   WHERE lp2.user.id = :userId AND lp2.lesson.course.id = l.course.id " +
           "   AND lp2.updatedAt = (" +
           "       SELECT MAX(lp3.updatedAt) FROM LessonProgress lp3 " +
           "       WHERE lp3.user.id = :userId AND lp3.lesson.course.id = l.course.id" +
           "   )" +
           ")")
    List<CourseLatestProgress> findLatestProgressForEachCourseByUserId(@Param("userId") Long userId, @Param("courseIds") List<Long> courseIds);
}
