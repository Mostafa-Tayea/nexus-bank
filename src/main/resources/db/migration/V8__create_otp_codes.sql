-- ============================================================
-- V8: Create otp_codes table
-- ============================================================

CREATE TABLE otp_codes (
    id           BINARY(16)    NOT NULL,
    code         VARCHAR(6)    NOT NULL,
    purpose      VARCHAR(255)  NOT NULL,
    expiry_time  DATETIME(6)   NOT NULL,
    verified     BIT(1)        NOT NULL DEFAULT 0,
    verified_at  DATETIME(6)   NULL,
    user_id      BINARY(16)    NOT NULL,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NULL,
    version      BIGINT        NULL,

    CONSTRAINT pk_otp_codes PRIMARY KEY (id),
    CONSTRAINT fk_otp_codes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_otp_codes_user_id ON otp_codes (user_id);
CREATE INDEX idx_otp_codes_expiry ON otp_codes (expiry_time);
