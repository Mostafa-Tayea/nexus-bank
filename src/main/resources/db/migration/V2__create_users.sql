-- ============================================================
-- V2: Create users table
-- ============================================================

CREATE TABLE users (
    id                  BINARY(16)    NOT NULL,
    first_name          VARCHAR(50)   NOT NULL,
    last_name           VARCHAR(50)   NOT NULL,
    email               VARCHAR(100)  NOT NULL,
    phone               VARCHAR(20)   NOT NULL,
    national_id         VARCHAR(14)   NOT NULL,
    password            VARCHAR(255)  NOT NULL,
    enabled             BIT(1)        NOT NULL DEFAULT 0,
    account_non_locked  BIT(1)        NOT NULL DEFAULT 1,
    failed_attempts     INT           NOT NULL DEFAULT 0,
    last_login          DATETIME(6)   NULL,
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NULL,
    version             BIGINT        NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone),
    CONSTRAINT uk_users_national_id UNIQUE (national_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_phone ON users (phone);
CREATE INDEX idx_users_national_id ON users (national_id);
