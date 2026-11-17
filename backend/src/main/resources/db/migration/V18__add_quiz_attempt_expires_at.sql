-- V18__add_quiz_attempt_expires_at.sql
-- Add persisted server-owned expires_at timestamp to quiz_attempts and index for efficient expiration queries

ALTER TABLE quiz_attempts ADD COLUMN expires_at DATETIME NULL;

-- Backfill expires_at for existing timed attempts where quiz has time_limit_minutes
UPDATE quiz_attempts qa
JOIN quizzes q ON qa.quiz_id = q.id
SET qa.expires_at = DATE_ADD(qa.started_at, INTERVAL q.time_limit_minutes MINUTE)
WHERE q.time_limit_minutes IS NOT NULL AND q.time_limit_minutes > 0;

CREATE INDEX idx_quiz_attempts_status_expires_at ON quiz_attempts(status, expires_at);
