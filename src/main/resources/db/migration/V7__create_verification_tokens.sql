-- ============================================================
-- V7: Create verification_tokens table
-- ============================================================

CREATE TABLE verification_tokens (
    id           BINARY(16)    NOT NULL,
    token        VARCHAR(500)  NOT NULL,
    expiry_date  DATETIME(6)   NOT NULL,
    verified     BIT(1)        NOT NULL DEFAULT 0,
    verified_at  DATETIME(6)   NULL,
    user_id      BINARY(16)    NOT NULL,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NULL,
    version      BIGINT        NULL,

    CONSTRAINT pk_verification_tokens PRIMARY KEY (id),
    CONSTRAINT uk_verification_tokens_token UNIQUE (token),
    CONSTRAINT fk_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_verification_tokens_user_id ON verification_tokens (user_id);
CREATE INDEX idx_verification_tokens_expiry ON verification_tokens (expiry_date);
