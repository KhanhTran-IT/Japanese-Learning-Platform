# Performance Profiling and Database Tuning Guide (#382)

## 1. Executive Summary

This document establishes the performance baseline, query profiling analysis, index justifications, N+1 query mitigations, connection pool configuration, and deployment assumptions for the Japanese Learning Platform API.

All optimizations in this document were implemented based on code inspection and query plan profiling rather than blind indexing.

---

## 2. High-Traffic Endpoints Profiling & Query Analysis

### 2.1 Courses Module
- **Endpoints:**
  - `GET /api/v1/courses` (public course catalog with level & type filters)
  - `GET /api/v1/courses/{slug}` (course detail with curriculum breakdown)
  - `GET /api/v1/courses/{courseId}/syllabus`
- **Profiling Analysis:**
  - `CoursePublicServiceImpl.getPublishedCourses` uses `@EntityGraph(attributePaths = {"teacher"})` for single-query joins, preventing N+1 on teacher lookups.
  - `CoursePublicServiceImpl.getCourseDetailBySlug` deliberately uses a two-phase query (course + sections, then lessons by sections) to avoid a Cartesian product `List<CourseSection> × List<Lesson>`.
  - **Identified Bottleneck:** `course_sections` table was missing a composite index on `(course_id, sort_order)`. Although MySQL automatically indexed the foreign key `course_id`, sorting by `sort_order ASC` resulted in an explicit `Using filesort` on large course curriculums.
  - **Resolution:** Added composite index `idx_course_sections_course_sort` on `(course_id, sort_order)` via Flyway `V11__performance_indexes.sql`.

### 2.2 Dashboard Module (Admin & Student)
- **Admin Dashboard (`GET /api/v1/admin/dashboard`):**
  - Runs 4 count queries (`totalUsers`, `totalCourses`, `totalLessons`, `totalEnrollments`) and 2 top-5 queries (`findTop5ByOrderByCreatedAtDesc`).
  - **Identified Bottleneck:** `getDashboardStats()` lacked a `@Transactional(readOnly = true)` boundary. Each repository call operated under autocommit mode with separate database round trips without a unified repeatable-read / read snapshot.
  - **Resolution:** Added `@Transactional(readOnly = true)` to `AdminDashboardServiceImpl.getDashboardStats()`.
- **Student Dashboard (`GET /api/v1/user/me/courses`, `GET /api/v1/user/me/progress`):**
  - `StudentDashboardServiceImpl.getMyCourses()` uses `CourseEnrollmentRepository.findByUserId` which already utilizes `@EntityGraph(attributePaths = {"course"})`.
  - Batch queries `progressRepository.countCompletedLessonsByCourseForUser(userId, courseIds)` and `progressRepository.findLatestProgressForEachCourseByUserId(userId, courseIds)` execute in O(1) queries rather than per-course loops.

### 2.3 Learning Progress Module
- **Endpoints:**
  - `POST /api/v1/learning/progress` (heartbeat / completion upsert)
  - `GET /api/v1/learning/courses/{courseId}/progress`
- **Profiling Analysis:**
  - `upsertLessonProgress` uses an atomic `UPDATE` followed by conditional `INSERT` if rows affected is 0, protected by unique constraint `uk_user_lesson(user_id, lesson_id)` from `V1__init_schema.sql`.
  - Subquery in `LessonProgressRepository.findLatestProgressForEachCourseByUserId` is constrained per user id and properly uses `idx_progress_user_lesson`.

### 2.4 Quiz Attempts Module (Critical Finding)
- **Endpoints:**
  - `GET /api/v1/quizzes/me/attempts` (student attempt history)
  - `GET /api/v1/quizzes/lesson/{lessonId}` (lesson quiz discovery)
- **Identified Bottlenecks:**
  1. **N+1 Query on `getMyAttempts()`:**
     - `attemptRepository.findByUserIdOrderByStartedAtDesc(user.getId())` fetched `QuizAttempt` instances without eagerly fetching `Quiz`.
     - In the response mapping, `a.getQuiz().getTitle()` and `a.getQuiz().getPassingScore()` triggered N additional SELECT queries for N attempts.
  2. **N+1 Query on `getLessonQuizzes(lessonId)`:**
     - For N published quizzes in a lesson, the code executed inside a loop:
       - `questionRepository.countByQuizId(quiz.getId())` (N queries)
       - `attemptRepository.findFirstByUserIdAndQuizIdOrderByStartedAtDesc(...)` (N queries)
       - `attemptRepository.countByUserIdAndQuizId(...)` (N queries)
     - Total queries: `1 + 3N`. For a lesson with 5 quizzes, 16 SQL queries were executed.
  3. **Filesort on Attempts Listing:**
     - Existing indexes were on `(user_id, quiz_id)` and `(user_id, quiz_id, attempt_number)`.
     - Querying `WHERE user_id = :userId ORDER BY started_at DESC` forced a filesort.
- **Resolutions:**
  1. Added `@EntityGraph(attributePaths = {"quiz"})` to `QuizAttemptRepository.findByUserIdOrderByStartedAtDesc`.
  2. Added batch methods to `QuestionRepository` and `QuizAttemptRepository`:
     - `countQuestionsByQuizIds(Collection<Long> quizIds)`
     - `countAttemptsByUserIdAndQuizIds(Long userId, Collection<Long> quizIds)`
     - `findByUserIdAndQuizIdInOrderByStartedAtDesc(Long userId, Collection<Long> quizIds)`
  3. Refactored `QuizLearningServiceImpl.getLessonQuizzes()` to fetch all counts and latest attempts in 3 batch queries, reducing database calls from `1 + 3N` to `4` constant queries.
  4. Added `@EntityGraph(attributePaths = {"course", "lesson", "lesson.course"})` to `QuizRepository.findByLessonIdAndStatus` to prevent lazy loading during access validation.
  5. Added index `idx_quiz_attempts_user_started ON quiz_attempts(user_id, started_at DESC)` in `V11__performance_indexes.sql`.

### 2.5 Leaderboard Module
- **Endpoints:**
  - `GET /api/v1/leaderboard` (all-time, weekly, monthly rankings)
- **Profiling Analysis:**
  - `UserScoreRepository.findTopByPeriod` uses `JOIN FETCH us.user`, preventing N+1 on user profile data.
  - Index `idx_scores_ranking ON user_scores(period_type, period_value, total_xp DESC)` from migration `V6` enables efficient index range scans for top-100 queries.
  - User activity log listing benefited from adding `idx_learning_activities_user_created ON learning_activities(user_id, created_at DESC)`.

### 2.6 Orders Module
- **Endpoints:**
  - `GET /api/v1/orders/my-orders`
- **Profiling Analysis:**
  - `OrderRepository.findByUserIdOrderByCreatedAtDesc` already uses `@EntityGraph(attributePaths = {"items", "items.course"})`.
  - Added index `idx_orders_user_created ON orders(user_id, created_at DESC)` to eliminate filesort when students view their order histories.

---

## 3. Database Migration V11 Index Summary

| Table | Index Name | Columns | Justification / Query Covered |
|---|---|---|---|
| `quiz_attempts` | `idx_quiz_attempts_user_started` | `(user_id, started_at DESC)` | Eliminates filesort for `getMyAttempts` (`WHERE user_id = ? ORDER BY started_at DESC`) |
| `course_sections` | `idx_course_sections_course_sort` | `(course_id, sort_order)` | Eliminates filesort for course curriculum retrieval (`WHERE course_id = ? ORDER BY sort_order ASC`) |
| `learning_activities` | `idx_learning_activities_user_created` | `(user_id, created_at DESC)` | Eliminates filesort for user learning activity timeline |
| `orders` | `idx_orders_user_created` | `(user_id, created_at DESC)` | Eliminates filesort for student order history (`WHERE user_id = ? ORDER BY created_at DESC`) |

---

## 4. Connection Pool Configuration (HikariCP)

### 4.1 Sizing Methodology
Using the PostgreSQL/HikariCP formula:
$$\text{pool\_size} = (\text{core\_count} \times 2) + \text{effective\_spindle\_count}$$

For a standard production node (2 vCPU container attached to managed MariaDB / NVMe SSD storage):
- Base calculation: $(2 \times 2) + 1 = 5$ connections per instance.
- Adding headroom for mixed OLTP traffic (read-heavy course browsing + write-burst quiz submissions): **15–20 connections** per API instance.

### 4.2 Configuration Parameters

```yaml
spring:
  datasource:
    hikari:
      pool-name: JapaneseLearningProdPool
      maximum-pool-size: ${HIKARI_MAX_POOL_SIZE:20}
      minimum-idle: ${HIKARI_MIN_IDLE:10}
      connection-timeout: ${HIKARI_CONNECTION_TIMEOUT:20000}    # 20s wait before throwing SQLException
      idle-timeout: ${HIKARI_IDLE_TIMEOUT:300000}               # 5 min idle retirement
      max-lifetime: ${HIKARI_MAX_LIFETIME:1200000}             # 20 min max connection lifespan (under MariaDB wait_timeout)
      leak-detection-threshold: ${HIKARI_LEAK_DETECTION:5000}   # 5s threshold logs stacktrace if connection not returned
```

### 4.3 Connection Leak Prevention
- `open-in-view: false` is enforced globally across all profiles (`application.yml`). Hibernate sessions close immediately when service transaction completes, returning connections to the pool before Jackson serialization begins.
- `leak-detection-threshold: 2000ms` in development / `5000ms` in production logs warning alerts if any thread retains a connection longer than normal request lifecycles.

---

## 5. Capacity Planning & Deployment Assumptions

| Metric | Target / Assumption | Rationale |
|---|---|---|
| **Daily Active Users (DAU)** | 1,000 – 5,000 users | Target for initial production launch |
| **Peak Concurrent Users** | 100 – 300 concurrent | Evening study peak hours (19:00 – 22:00) |
| **Target Throughput** | 500 req/sec | Peak load handled by 2 API replicas |
| **p95 Latency SLA** | < 150 ms | Course catalog, lesson streaming progress, quiz listing |
| **p99 Latency SLA** | < 300 ms | Complex queries (Leaderboard, Quiz submission scoring) |
| **Database Limits** | Max 100 connections on MariaDB | 2 API pods × 20 pool + 10 worker/cron + 50 buffer |
| **Page Size Bounds** | Max 100 items (`max-page-size: 100`) | Enforced in `application.yml` via `spring.data.web.pageable` |
