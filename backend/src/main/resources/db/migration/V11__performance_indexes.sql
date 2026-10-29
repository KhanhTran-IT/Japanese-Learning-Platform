-- ============================================================
-- V11: Performance Indexes based on query profiling
-- Covers:
-- 1. quiz_attempts: eliminate filesort on findByUserIdOrderByStartedAtDesc
-- 2. course_sections: eliminate filesort on findByCourseIdOrderBySortOrderAsc
-- 3. learning_activities: eliminate filesort on findByUserIdOrderByCreatedAtDesc
-- 4. orders: eliminate filesort on findByUserIdOrderByCreatedAtDesc
-- ============================================================

CREATE INDEX idx_quiz_attempts_user_started ON quiz_attempts(user_id, started_at DESC);
CREATE INDEX idx_course_sections_course_sort ON course_sections(course_id, sort_order);
CREATE INDEX idx_learning_activities_user_created ON learning_activities(user_id, created_at DESC);
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at DESC);
