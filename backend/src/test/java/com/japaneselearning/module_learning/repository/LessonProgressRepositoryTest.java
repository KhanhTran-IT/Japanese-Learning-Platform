package com.japaneselearning.module_learning.repository;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_learning.entity.LessonProgress;
import com.japaneselearning.module_user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-level tests for {@link LessonProgressRepository} custom JPA queries.
 * Uses @DataJpaTest with H2 in-memory database — only the JPA slice is loaded,
 * so no security config, no seeders, no web layer.
 */
@DataJpaTest
@ActiveProfiles("test")
class LessonProgressRepositoryTest {

    @Autowired
    private LessonProgressRepository progressRepository;

    @Autowired
    private EntityManager em;

    // Shared test fixtures
    private User student;
    private User otherStudent;
    private Course courseA;
    private Course courseB;
    private Lesson lessonA1;
    private Lesson lessonA2;
    private Lesson lessonA3;
    private Lesson lessonB1;

    @BeforeEach
    void setUp() {
        // -- Users --
        student = User.builder()
                .fullName("Student One")
                .email("student1@test.com")
                .passwordHash("hashed")
                .build();
        em.persist(student);

        otherStudent = User.builder()
                .fullName("Student Two")
                .email("student2@test.com")
                .passwordHash("hashed")
                .build();
        em.persist(otherStudent);

        // -- Teacher --
        User teacher = User.builder()
                .fullName("Teacher")
                .email("teacher@test.com")
                .passwordHash("hashed")
                .build();
        em.persist(teacher);

        // -- Course A with 3 lessons --
        courseA = Course.builder()
                .title("Course A")
                .slug("course-a")
                .level(CourseLevel.N5)
                .courseType(CourseType.FREE)
                .status(CourseStatus.PUBLISHED)
                .teacher(teacher)
                .build();
        em.persist(courseA);

        CourseSection sectionA = CourseSection.builder()
                .course(courseA)
                .title("Section A1")
                .build();
        em.persist(sectionA);

        lessonA1 = buildLesson(courseA, sectionA, "Lesson A1", "lesson-a1", 0);
        lessonA2 = buildLesson(courseA, sectionA, "Lesson A2", "lesson-a2", 1);
        lessonA3 = buildLesson(courseA, sectionA, "Lesson A3", "lesson-a3", 2);
        em.persist(lessonA1);
        em.persist(lessonA2);
        em.persist(lessonA3);

        // -- Course B with 1 lesson --
        courseB = Course.builder()
                .title("Course B")
                .slug("course-b")
                .level(CourseLevel.N4)
                .courseType(CourseType.PAID)
                .status(CourseStatus.PUBLISHED)
                .teacher(teacher)
                .build();
        em.persist(courseB);

        CourseSection sectionB = CourseSection.builder()
                .course(courseB)
                .title("Section B1")
                .build();
        em.persist(sectionB);

        lessonB1 = buildLesson(courseB, sectionB, "Lesson B1", "lesson-b1", 0);
        em.persist(lessonB1);

        em.flush();
        em.clear();
    }

    // ========================= updateProgressAtomically =========================

    @Nested
    @DisplayName("updateProgressAtomically")
    class UpdateProgressAtomically {

        @Test
        @DisplayName("should increase watchedPercent when new value is higher")
        void monotonic_increase() {
            // Seed initial progress at 30%
            LessonProgress initial = LessonProgress.builder()
                    .user(em.find(User.class, student.getId()))
                    .lesson(em.find(Lesson.class, lessonA1.getId()))
                    .watchedPercent(30.0)
                    .isCompleted(false)
                    .build();
            em.persist(initial);
            em.flush();
            em.clear();

            int rows = progressRepository.updateProgressAtomically(
                    student.getId(), lessonA1.getId(), 75.0, false, null);

            assertThat(rows).isEqualTo(1);

            LessonProgress updated = progressRepository
                    .findByUserIdAndLessonId(student.getId(), lessonA1.getId())
                    .orElseThrow();
            assertThat(updated.getWatchedPercent()).isEqualTo(75.0);
            assertThat(updated.getIsCompleted()).isFalse();
        }

        @Test
        @DisplayName("should NOT decrease watchedPercent when new value is lower (monotonic)")
        void monotonic_no_decrease() {
            LessonProgress initial = LessonProgress.builder()
                    .user(em.find(User.class, student.getId()))
                    .lesson(em.find(Lesson.class, lessonA1.getId()))
                    .watchedPercent(80.0)
                    .isCompleted(false)
                    .build();
            em.persist(initial);
            em.flush();
            em.clear();

            progressRepository.updateProgressAtomically(
                    student.getId(), lessonA1.getId(), 40.0, false, null);

            LessonProgress unchanged = progressRepository
                    .findByUserIdAndLessonId(student.getId(), lessonA1.getId())
                    .orElseThrow();
            assertThat(unchanged.getWatchedPercent()).isEqualTo(80.0);
        }

        @Test
        @DisplayName("should set isCompleted to true and record completedAt timestamp")
        void marks_completed() {
            LessonProgress initial = LessonProgress.builder()
                    .user(em.find(User.class, student.getId()))
                    .lesson(em.find(Lesson.class, lessonA1.getId()))
                    .watchedPercent(50.0)
                    .isCompleted(false)
                    .build();
            em.persist(initial);
            em.flush();
            em.clear();

            LocalDateTime completedAt = LocalDateTime.of(2026, 8, 10, 14, 0);
            progressRepository.updateProgressAtomically(
                    student.getId(), lessonA1.getId(), 100.0, true, completedAt);

            LessonProgress completed = progressRepository
                    .findByUserIdAndLessonId(student.getId(), lessonA1.getId())
                    .orElseThrow();
            assertThat(completed.getWatchedPercent()).isEqualTo(100.0);
            assertThat(completed.getIsCompleted()).isTrue();
            assertThat(completed.getCompletedAt()).isEqualTo(completedAt);
        }

        @Test
        @DisplayName("should NOT un-complete a lesson once marked completed")
        void cannot_uncomplete() {
            LessonProgress initial = LessonProgress.builder()
                    .user(em.find(User.class, student.getId()))
                    .lesson(em.find(Lesson.class, lessonA1.getId()))
                    .watchedPercent(100.0)
                    .isCompleted(true)
                    .completedAt(LocalDateTime.of(2026, 8, 1, 10, 0))
                    .build();
            em.persist(initial);
            em.flush();
            em.clear();

            progressRepository.updateProgressAtomically(
                    student.getId(), lessonA1.getId(), 50.0, false, null);

            LessonProgress stillCompleted = progressRepository
                    .findByUserIdAndLessonId(student.getId(), lessonA1.getId())
                    .orElseThrow();
            assertThat(stillCompleted.getIsCompleted()).isTrue();
            assertThat(stillCompleted.getWatchedPercent()).isEqualTo(100.0);
            assertThat(stillCompleted.getCompletedAt())
                    .isEqualTo(LocalDateTime.of(2026, 8, 1, 10, 0));
        }

        @Test
        @DisplayName("should return 0 rows affected for non-existent user-lesson pair")
        void no_match_returns_zero() {
            int rows = progressRepository.updateProgressAtomically(
                    999L, 999L, 50.0, false, null);
            assertThat(rows).isEqualTo(0);
        }
    }

    // =================== countCompletedLessonsByCourseForUser ===================

    @Nested
    @DisplayName("countCompletedLessonsByCourseForUser")
    class CountCompletedLessons {

        @Test
        @DisplayName("should count completed lessons grouped by course")
        void grouped_counts() {
            User u = em.find(User.class, student.getId());

            // Course A: 2 completed, 1 not
            em.persist(progress(u, lessonA1, 100.0, true));
            em.persist(progress(u, lessonA2, 100.0, true));
            em.persist(progress(u, lessonA3, 50.0, false));

            // Course B: 1 completed
            em.persist(progress(u, lessonB1, 100.0, true));

            em.flush();
            em.clear();

            List<LessonProgressRepository.CourseProgressCount> counts =
                    progressRepository.countCompletedLessonsByCourseForUser(
                            student.getId(),
                            List.of(courseA.getId(), courseB.getId()));

            assertThat(counts).hasSize(2);

            LessonProgressRepository.CourseProgressCount countA = counts.stream()
                    .filter(c -> c.getCourseId().equals(courseA.getId()))
                    .findFirst().orElseThrow();
            assertThat(countA.getCompletedCount()).isEqualTo(2L);

            LessonProgressRepository.CourseProgressCount countB = counts.stream()
                    .filter(c -> c.getCourseId().equals(courseB.getId()))
                    .findFirst().orElseThrow();
            assertThat(countB.getCompletedCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should not include other student's completions")
        void scoped_to_user() {
            User u = em.find(User.class, student.getId());
            User other = em.find(User.class, otherStudent.getId());

            em.persist(progress(u, lessonA1, 100.0, true));
            em.persist(progress(other, lessonA2, 100.0, true));
            em.flush();
            em.clear();

            List<LessonProgressRepository.CourseProgressCount> counts =
                    progressRepository.countCompletedLessonsByCourseForUser(
                            student.getId(), List.of(courseA.getId()));

            assertThat(counts).hasSize(1);
            assertThat(counts.get(0).getCompletedCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return empty list when no lessons are completed")
        void no_completions() {
            User u = em.find(User.class, student.getId());
            em.persist(progress(u, lessonA1, 50.0, false));
            em.flush();
            em.clear();

            List<LessonProgressRepository.CourseProgressCount> counts =
                    progressRepository.countCompletedLessonsByCourseForUser(
                            student.getId(), List.of(courseA.getId()));

            assertThat(counts).isEmpty();
        }
    }

    // ================== findLatestProgressForEachCourseByUserId ==================

    @Nested
    @DisplayName("findLatestProgressForEachCourseByUserId")
    class FindLatestProgress {

        @Test
        @DisplayName("should return lesson with latest updatedAt per course")
        void latest_per_course() {
            User u = em.find(User.class, student.getId());

            // Lesson A1 updated earlier
            LessonProgress pA1 = progress(u, lessonA1, 30.0, false);
            em.persist(pA1);
            em.flush();

            // Lesson A2 updated later (we force updatedAt via native query)
            LessonProgress pA2 = progress(u, lessonA2, 60.0, false);
            em.persist(pA2);
            em.flush();

            // Course B: only one lesson
            LessonProgress pB1 = progress(u, lessonB1, 80.0, false);
            em.persist(pB1);
            em.flush();
            em.clear();

            List<LessonProgressRepository.CourseLatestProgress> result =
                    progressRepository.findLatestProgressForEachCourseByUserId(
                            student.getId(),
                            List.of(courseA.getId(), courseB.getId()));

            assertThat(result).hasSize(2);

            // For course A, the latest should be lessonA2 (higher ID, later persist = later updatedAt)
            LessonProgressRepository.CourseLatestProgress latestA = result.stream()
                    .filter(r -> r.getCourseId().equals(courseA.getId()))
                    .findFirst().orElseThrow();
            assertThat(latestA.getLessonId()).isEqualTo(lessonA2.getId());
            assertThat(latestA.getLessonName()).isEqualTo("Lesson A2");
            assertThat(latestA.getLessonSlug()).isEqualTo("lesson-a2");

            // For course B, only one lesson
            LessonProgressRepository.CourseLatestProgress latestB = result.stream()
                    .filter(r -> r.getCourseId().equals(courseB.getId()))
                    .findFirst().orElseThrow();
            assertThat(latestB.getLessonId()).isEqualTo(lessonB1.getId());
        }
    }

    // ==================== Derived query methods ====================

    @Nested
    @DisplayName("Derived query methods")
    class DerivedQueries {

        @Test
        @DisplayName("findByUserIdAndLessonId should return correct progress")
        void findByUserAndLesson() {
            User u = em.find(User.class, student.getId());
            em.persist(progress(u, lessonA1, 45.0, false));
            em.flush();
            em.clear();

            var found = progressRepository
                    .findByUserIdAndLessonId(student.getId(), lessonA1.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getWatchedPercent()).isEqualTo(45.0);
        }

        @Test
        @DisplayName("countByUserIdAndIsCompletedTrue should count across all courses")
        void countAllCompleted() {
            User u = em.find(User.class, student.getId());
            em.persist(progress(u, lessonA1, 100.0, true));
            em.persist(progress(u, lessonA2, 100.0, true));
            em.persist(progress(u, lessonA3, 50.0, false));
            em.persist(progress(u, lessonB1, 100.0, true));
            em.flush();
            em.clear();

            int count = progressRepository.countByUserIdAndIsCompletedTrue(student.getId());
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("countByUserIdAndLessonCourseIdAndIsCompletedTrue should scope to single course")
        void countCompletedInCourse() {
            User u = em.find(User.class, student.getId());
            em.persist(progress(u, lessonA1, 100.0, true));
            em.persist(progress(u, lessonA2, 100.0, true));
            em.persist(progress(u, lessonB1, 100.0, true));
            em.flush();
            em.clear();

            int count = progressRepository.countByUserIdAndLessonCourseIdAndIsCompletedTrue(
                    student.getId(), courseA.getId());
            assertThat(count).isEqualTo(2);
        }
    }

    // ========================= helpers =========================

    private Lesson buildLesson(Course course, CourseSection section, String title, String slug, int order) {
        return Lesson.builder()
                .course(course)
                .section(section)
                .title(title)
                .slug(slug)
                .sortOrder(order)
                .build();
    }

    private LessonProgress progress(User user, Lesson lesson, double percent, boolean completed) {
        Lesson managedLesson = em.find(Lesson.class, lesson.getId());
        return LessonProgress.builder()
                .user(user)
                .lesson(managedLesson)
                .watchedPercent(percent)
                .isCompleted(completed)
                .completedAt(completed ? LocalDateTime.now() : null)
                .build();
    }
}
