-- V19__add_payment_retention_index.sql
-- Add index on payments(created_at) for efficient retention cleanup and audit range queries

CREATE INDEX idx_payments_created_at ON payments(created_at);
