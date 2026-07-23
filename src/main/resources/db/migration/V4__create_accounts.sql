-- ============================================================
-- V4: Create accounts table
-- ============================================================

CREATE TABLE accounts (
    id                       BINARY(16)     NOT NULL,
    account_number           VARCHAR(20)    NOT NULL,
    iban                     VARCHAR(34)    NOT NULL,
    balance                  DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    currency                 VARCHAR(3)     NOT NULL DEFAULT 'EGP',
    type                     VARCHAR(255)   NOT NULL,
    status                   VARCHAR(255)   NOT NULL DEFAULT 'ACTIVE',
    daily_transfer_limit     DECIMAL(19, 4) NULL,
    daily_transferred_amount DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    user_id                  BINARY(16)     NOT NULL,
    created_at               DATETIME(6)    NOT NULL,
    updated_at               DATETIME(6)    NULL,
    version                  BIGINT         NULL,

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uk_accounts_number UNIQUE (account_number),
    CONSTRAINT uk_accounts_iban UNIQUE (iban),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_accounts_balance CHECK (balance >= 0),
    CONSTRAINT chk_accounts_daily_transferred CHECK (daily_transferred_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
CREATE INDEX idx_accounts_account_number ON accounts (account_number);
