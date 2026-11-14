-- V16: Create append-only audit trail table for sensitive security and business actions
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT NULL,
    actor_email VARCHAR(150) NULL,
    actor_role VARCHAR(100) NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NULL,
    target_id VARCHAR(64) NULL,
    outcome VARCHAR(20) NOT NULL,
    error_message VARCHAR(500) NULL,
    correlation_id VARCHAR(64) NULL,
    client_ip VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    metadata TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_actor ON audit_logs (actor_id, created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs (action, created_at);
CREATE INDEX idx_audit_logs_target ON audit_logs (target_type, target_id);
CREATE INDEX idx_audit_logs_correlation ON audit_logs (correlation_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
