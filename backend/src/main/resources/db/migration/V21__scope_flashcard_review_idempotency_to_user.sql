-- V21__scope_flashcard_review_idempotency_to_user.sql
-- Scope flashcard review idempotency_key uniqueness to (user_id, idempotency_key)
-- instead of a global unique constraint on idempotency_key.
-- Uses column swap so dropping the unnamed inline UNIQUE constraint works
-- identically on both MariaDB and H2 (MODE=MySQL).

ALTER TABLE flashcard_review_logs ADD COLUMN idempotency_key_tmp VARCHAR(255) NULL;
UPDATE flashcard_review_logs SET idempotency_key_tmp = idempotency_key;
ALTER TABLE flashcard_review_logs DROP COLUMN idempotency_key;
ALTER TABLE flashcard_review_logs CHANGE COLUMN idempotency_key_tmp idempotency_key VARCHAR(255) NOT NULL;
ALTER TABLE flashcard_review_logs ADD CONSTRAINT uk_flashcard_review_user_idempotency UNIQUE (user_id, idempotency_key);
