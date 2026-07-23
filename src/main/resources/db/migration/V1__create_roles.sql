-- ============================================================
-- V1: Create roles table
-- ============================================================

CREATE TABLE roles (
    id              BINARY(16)    NOT NULL,
    name            VARCHAR(255)  NOT NULL,
    description     VARCHAR(255)  NULL,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NULL,
    version         BIGINT        NULL,

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
