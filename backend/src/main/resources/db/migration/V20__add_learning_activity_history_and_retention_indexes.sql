-- ============================================================
-- V20: Indexes for learning activity deterministic pagination,
-- retention cleanup, and aggregation optimization
-- Covers:
-- 1. idx_learning_activities_user_created_id: covering index for deterministic sorting (created_at DESC, id DESC) per user
-- 2. idx_learning_activities_created_at: index for efficient retention purge / archival of historical events
-- ============================================================

CREATE INDEX idx_learning_activities_user_created_id ON learning_activities(user_id, created_at DESC, id DESC);
CREATE INDEX idx_learning_activities_created_at ON learning_activities(created_at);
