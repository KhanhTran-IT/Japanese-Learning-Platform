-- V15__add_user_token_version.sql
-- Add token_version column to users table to support bounded-time/immediate token invalidation

ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 1;
