-- V10__add_refresh_token_lifecycle_indexes.sql
-- Migration to add performance and safety indexes for refresh token lifecycle management.
-- 1. idx_refresh_tokens_user_revoked: Optimizes session revocation by user_id and active/revoked status
--    (triggered during password changes, account locks, and family token reuse detection).
-- 2. idx_refresh_tokens_expired_at: Optimizes scheduled cleanup queries scanning for expired tokens.
-- 3. idx_refresh_tokens_revoked: Optimizes filtering and scheduled cleanup of revoked tokens.

CREATE INDEX idx_refresh_tokens_user_revoked ON refresh_tokens (user_id, revoked);
CREATE INDEX idx_refresh_tokens_expired_at ON refresh_tokens (expired_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens (revoked);
