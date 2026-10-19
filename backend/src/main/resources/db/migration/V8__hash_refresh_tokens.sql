-- V8__hash_refresh_tokens.sql
-- Migration to store refresh tokens hashed at rest (SHA-256) instead of plaintext.

-- 1. Add token_hash column (if not already present from a partial previous run)
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS token_hash VARCHAR(64) NULL AFTER token;

-- 2. Migrate existing plaintext records: compute SHA-256 hex digest
UPDATE refresh_tokens
SET token_hash = LOWER(SHA2(token, 256))
WHERE token IS NOT NULL AND (token_hash IS NULL OR token_hash = '');

-- 3. Delete any invalid records where token_hash could not be generated
DELETE FROM refresh_tokens WHERE token_hash IS NULL;

-- 4. Set token_hash as NOT NULL
ALTER TABLE refresh_tokens MODIFY COLUMN token_hash VARCHAR(64) NOT NULL;

-- 5. Add unique index on token_hash if it doesn't exist
CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_tokens_token_hash ON refresh_tokens (token_hash);

-- 6. Drop the unique constraint on plaintext token.
--    The index may be named 'token' (manual) or a Hibernate-generated name like 'UKghpmfn...'
--    MariaDB 10.5.2+ supports ALTER TABLE ... DROP INDEX IF EXISTS
ALTER TABLE refresh_tokens DROP INDEX IF EXISTS token;
ALTER TABLE refresh_tokens DROP INDEX IF EXISTS UKghpmfn23vmxfu3spu3lfg4r2d;

-- 7. Allow token column to be NULL
ALTER TABLE refresh_tokens MODIFY COLUMN token VARCHAR(500) NULL;

-- 8. Wipe plaintext token data for existing records
UPDATE refresh_tokens SET token = NULL;
