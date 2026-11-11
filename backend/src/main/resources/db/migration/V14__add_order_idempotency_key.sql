-- V14__add_order_idempotency_key.sql
-- Add idempotency_key column and unique constraint scoped to user_id

ALTER TABLE orders ADD COLUMN idempotency_key VARCHAR(255) NULL;
ALTER TABLE orders ADD CONSTRAINT uq_orders_user_idempotency UNIQUE (user_id, idempotency_key);
