-- ============================================================
-- V10: Create audit_logs table
-- ============================================================

CREATE TABLE audit_logs (
    id                BINARY(16)     NOT NULL,
    event_type        VARCHAR(100)   NOT NULL,
    action            VARCHAR(255)   NOT NULL,
    username          VARCHAR(100)   NULL,
    user_id           BINARY(16)     NULL,
    ip_address        VARCHAR(45)    NULL,
    device            VARCHAR(255)   NULL,
    http_method       VARCHAR(10)    NULL,
    endpoint          VARCHAR(255)   NULL,
    request_id        VARCHAR(100)   NULL,
    reference_number  VARCHAR(50)    NULL,
    timestamp         DATETIME(6)    NOT NULL,
    result            VARCHAR(20)    NULL,
    details           VARCHAR(1000)  NULL,
    created_at        DATETIME(6)    NOT NULL,
    updated_at        DATETIME(6)    NULL,
    version           BIGINT         NULL,

    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_event_type ON audit_logs (event_type);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs (timestamp);
