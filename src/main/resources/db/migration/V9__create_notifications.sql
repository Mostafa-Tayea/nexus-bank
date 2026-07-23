-- ============================================================
-- V9: Create notifications table
-- ============================================================

CREATE TABLE notifications (
    id             BINARY(16)     NOT NULL,
    title          VARCHAR(150)   NOT NULL,
    message        VARCHAR(1000)  NOT NULL,
    type           VARCHAR(255)   NOT NULL,
    status         VARCHAR(255)   NOT NULL DEFAULT 'PENDING',
    sent_at        DATETIME(6)    NULL,
    read_at        DATETIME(6)    NULL,
    retry_count    INT            NOT NULL DEFAULT 0,
    failure_reason VARCHAR(500)   NULL,
    user_id        BINARY(16)     NOT NULL,
    created_at     DATETIME(6)    NOT NULL,
    updated_at     DATETIME(6)    NULL,
    version        BIGINT         NULL,

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);
