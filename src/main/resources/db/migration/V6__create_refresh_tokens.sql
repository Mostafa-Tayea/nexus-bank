-- ============================================================
-- V6: Create refresh_tokens table
-- ============================================================

CREATE TABLE refresh_tokens (
    id           BINARY(16)    NOT NULL,
    token        VARCHAR(500)  NOT NULL,
    expiry_date  DATETIME(6)   NOT NULL,
    revoked      BIT(1)        NOT NULL DEFAULT 0,
    user_id      BINARY(16)    NOT NULL,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NULL,
    version      BIGINT        NULL,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens (expiry_date);
