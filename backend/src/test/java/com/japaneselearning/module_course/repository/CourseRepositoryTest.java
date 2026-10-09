package com.japaneselearning.module_course.repository;

import com.japaneselearning.module_course.entity.Course;
import com.japaneselearning.module_course.entity.CourseSection;
import com.japaneselearning.module_course.entity.Lesson;
import com.japaneselearning.module_course.enums.CourseLevel;
import com.japaneselearning.module_course.enums.CourseStatus;
import com.japaneselearning.module_course.enums.CourseType;
import com.japaneselearning.module_user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-level tests for {@link CourseRepository} custom JPA queries.
 * Uses @DataJpaTest with H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EntityManager em;

    private User teacher;

    @BeforeEach
    void setUp() {
        teacher = User.builder()
                .fullName("Sensei Tanaka")
                .email("sensei@test.com")
                .passwordHash("hashed")
                .build();
        em.persist(teacher);
        em.flush();
    }

    // ========================= incrementTotalStudents =========================

    @Nested
    @DisplayName("incrementTotalStudents")
    class IncrementTotalStudents {

        @Test
        @DisplayName("should atomically increment totalStudents by 1")
        void increments_by_one() {
            Course course = persistCourse("Increment Test", "inc-test",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.PUBLISHED);

            assertThat(course.getTotalStudents()).isEqualTo(0);

            courseRepository.incrementTotalStudents(course.getId());
            em.flush();
            em.clear();

            Course reloaded = courseRepository.findById(course.getId()).orElseThrow();
            assertThat(reloaded.getTotalStudents()).isEqualTo(1);
        }

        @Test
        @DisplayName("should increment correctly when called multiple times")
        void increments_multiple() {
            Course course = persistCourse("Multi Inc", "multi-inc",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.PUBLISHED);

            courseRepository.incrementTotalStudents(course.getId());
            courseRepository.incrementTotalStudents(course.getId());
            courseRepository.incrementTotalStudents(course.getId());
            em.flush();
            em.clear();

            Course reloaded = courseRepository.findById(course.getId()).orElseThrow();
            assertThat(reloaded.getTotalStudents()).isEqualTo(3);
        }

        @Test
        @DisplayName("should not affect other courses")
        void isolated_increment() {
            Course courseX = persistCourse("Course X", "course-x",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.PUBLISHED);
            Course courseY = persistCourse("Course Y", "course-y",
                    CourseLevel.N4, CourseType.PAID, CourseStatus.PUBLISHED);

            courseRepository.incrementTotalStudents(courseX.getId());
            em.flush();
            em.clear();

            Course reloadedY = courseRepository.findById(courseY.getId()).orElseThrow();
            assertThat(reloadedY.getTotalStudents()).isEqualTo(0);
        }
    }

    // ========================= searchPublishedCourses =========================

    @Nested
    @DisplayName("searchPublishedCourses")
    class SearchPublishedCourses {

        @BeforeEach
        void seedCourses() {
            persistCourse("Japanese N5 Basics", "n5-basics",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.PUBLISHED);
            persistCourse("Japanese N4 Grammar", "n4-grammar",
                    CourseLevel.N4, CourseType.PAID, CourseStatus.PUBLISHED);
            persistCourse("Japanese N3 Advanced Reading", "n3-reading",
                    CourseLevel.N3, CourseType.PAID, CourseStatus.PUBLISHED);
            persistCourse("DRAFT Course N5", "draft-n5",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.DRAFT);
            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("should return all published courses when all filters are null")
        void no_filters() {
            Page<Course> result = courseRepository.searchPublishedCourses(
                    null, null, null, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("should exclude DRAFT courses")
        void excludes_draft() {
            Page<Course> result = courseRepository.searchPublishedCourses(
                    CourseLevel.N5, null, null, PageRequest.of(0, 10));

            assertThat(result.getContent())
                    .allMatch(c -> c.getStatus() == CourseStatus.PUBLISHED);
            // Only "Japanese N5 Basics" should match (DRAFT one excluded)
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getSlug()).isEqualTo("n5-basics");
        }

        @Test
        @DisplayName("should filter by level")
        void filter_by_level() {
            Page<Course> result = courseRepository.searchPublishedCourses(
                    CourseLevel.N4, null, null, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Japanese N4 Grammar");
        }

        @Test
        @DisplayName("should filter by courseType")
        void filter_by_type() {
            Page<Course> result = courseRepository.searchPublishedCourses(
                    null, CourseType.FREE, null, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getCourseType()).isEqualTo(CourseType.FREE);
        }

        @Test
        @DisplayName("should filter by keyword in title (case insensitive)")
        void filter_by_keyword_title() {
            Page<Course> result = courseRepository.searchPublishedCourses(
                    null, null, "grammar", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getSlug()).isEqualTo("n4-grammar");
        }

        @Test
        @DisplayName("should filter by keyword in shortDescription")
        void filter_by_keyword_description() {
            // Update a course's short description
            Course n3 = courseRepository.findBySlug("n3-reading").orElseThrow();
            n3.setShortDescription("This course covers kanji reading techniques");
            em.persist(n3);
            em.flush();
            em.clear();

            Page<Course> result = courseRepository.searchPublishedCourses(
                    null, null, "kanji", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getSlug()).isEqualTo("n3-reading");
        }

        @Test
        @DisplayName("should combine level + courseType + keyword filters")
        void combined_filters() {
            Page<Course> result = courseRepository.searchPublishedCourses(
                    CourseLevel.N4, CourseType.PAID, "grammar", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getSlug()).isEqualTo("n4-grammar");
        }

        @Test
        @DisplayName("should return empty page when no courses match")
        void no_match() {
            Page<Course> result = courseRepository.searchPublishedCourses(
                    CourseLevel.N1, null, null, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("should respect pagination")
        void pagination() {
            Page<Course> page0 = courseRepository.searchPublishedCourses(
                    null, null, null, PageRequest.of(0, 2));

            assertThat(page0.getContent()).hasSize(2);
            assertThat(page0.getTotalElements()).isEqualTo(3);
            assertThat(page0.getTotalPages()).isEqualTo(2);

            Page<Course> page1 = courseRepository.searchPublishedCourses(
                    null, null, null, PageRequest.of(1, 2));

            assertThat(page1.getContent()).hasSize(1);
        }
    }

    // ========================= findBySlug / existsBySlug =========================

    @Nested
    @DisplayName("Slug queries")
    class SlugQueries {

        @Test
        @DisplayName("findBySlug should return course by unique slug")
        void findBySlug() {
            persistCourse("Slug Course", "unique-slug",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.PUBLISHED);
            em.flush();
            em.clear();

            Optional<Course> found = courseRepository.findBySlug("unique-slug");
            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Slug Course");
        }

        @Test
        @DisplayName("existsBySlug should return true for existing slug")
        void existsBySlug_true() {
            persistCourse("Existing", "existing-slug",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.PUBLISHED);
            em.flush();
            em.clear();

            assertThat(courseRepository.existsBySlug("existing-slug")).isTrue();
            assertThat(courseRepository.existsBySlug("nonexistent")).isFalse();
        }
    }

    // ========================= findBySlugAndStatus =========================

    @Nested
    @DisplayName("findBySlugAndStatus")
    class FindBySlugAndStatus {

        @Test
        @DisplayName("should return course only when status matches")
        void status_filter() {
            persistCourse("Published One", "pub-one",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.PUBLISHED);
            persistCourse("Draft One", "draft-one",
                    CourseLevel.N5, CourseType.FREE, CourseStatus.DRAFT);
            em.flush();
            em.clear();

            assertThat(courseRepository.findBySlugAndStatus("pub-one", CourseStatus.PUBLISHED))
                    .isPresent();
            assertThat(courseRepository.findBySlugAndStatus("draft-one", CourseStatus.PUBLISHED))
                    .isEmpty();
            assertThat(courseRepository.findBySlugAndStatus("draft-one", CourseStatus.DRAFT))
                    .isPresent();
        }
    }

    // ========================= findTop5ByOrderByCreatedAtDesc =========================

    @Nested
    @DisplayName("findTop5ByOrderByCreatedAtDesc")
    class FindTop5 {

        @Test
        @DisplayName("should return at most 5 courses ordered by newest first")
        void top5_newest() {
            for (int i = 1; i <= 7; i++) {
                persistCourse("Course " + i, "course-" + i,
                        CourseLevel.N5, CourseType.FREE, CourseStatus.PUBLISHED);
            }
            em.flush();
            em.clear();

            List<Course> top5 = courseRepository.findTop5ByOrderByCreatedAtDesc();
            assertThat(top5).hasSize(5);
        }
    }

    // ========================= helpers =========================

    private Course persistCourse(String title, String slug, CourseLevel level,
                                  CourseType type, CourseStatus status) {
        Course course = Course.builder()
                .title(title)
                .slug(slug)
                .level(level)
                .courseType(type)
                .status(status)
                .teacher(teacher)
                .build();
        em.persist(course);
        return course;
    }
}
