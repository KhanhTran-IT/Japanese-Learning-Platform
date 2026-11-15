-- V17__add_learning_activity_unique_constraint.sql
-- Add unique constraint on learning_activities for reference-based activities idempotency

ALTER TABLE learning_activities
ADD CONSTRAINT uk_user_activity_reference UNIQUE (user_id, activity_type, reference_id);
