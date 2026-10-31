-- V12__add_order_pagination_index.sql
-- Composite index for student order history deterministic pagination (user_id, created_at DESC, id DESC)

CREATE INDEX idx_orders_user_created_id ON orders(user_id, created_at DESC, id DESC);
