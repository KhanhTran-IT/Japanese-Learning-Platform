-- ============================================================
-- V3: Add attempt_number to quiz_attempts for concurrency safety
-- ============================================================

ALTER TABLE quiz_attempts
ADD COLUMN attempt_number INT NOT NULL DEFAULT 1;

-- Thêm Unique Constraint để tránh race condition khi tạo attempt
ALTER TABLE quiz_attempts
ADD CONSTRAINT uk_quiz_attempts_user_quiz_number UNIQUE (user_id, quiz_id, attempt_number);
