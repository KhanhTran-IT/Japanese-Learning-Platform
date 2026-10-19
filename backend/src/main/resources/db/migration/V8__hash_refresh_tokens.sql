-- V8__hash_refresh_tokens.sql
-- Migration to store refresh tokens hashed at rest (SHA-256) instead of plaintext.

-- 1. Add token_hash column
ALTER TABLE refresh_tokens ADD COLUMN token_hash VARCHAR(64) NULL AFTER token;

-- 2. Migrate existing plaintext records: compute SHA-256 hex digest using SHA2(token, 256)
UPDATE refresh_tokens 
SET token_hash = LOWER(SHA2(token, 256)) 
WHERE token IS NOT NULL AND (token_hash IS NULL OR token_hash = '');

-- 3. Delete any invalid records where token_hash could not be generated (if any)
DELETE FROM refresh_tokens WHERE token_hash IS NULL;

-- 4. Set token_hash as NOT NULL and add unique index
ALTER TABLE refresh_tokens MODIFY COLUMN token_hash VARCHAR(64) NOT NULL;
ALTER TABLE refresh_tokens ADD CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash);

-- 5. Drop the unique constraint on plaintext token and allow null
ALTER TABLE refresh_tokens DROP INDEX token;
ALTER TABLE refresh_tokens MODIFY COLUMN token VARCHAR(500) NULL;

-- 6. Wipe plaintext token data for existing records to ensure zero plaintext tokens at rest
UPDATE refresh_tokens SET token = NULL;
