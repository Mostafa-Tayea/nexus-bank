-- ============================================================
-- V5: Create transactions table
-- ============================================================

CREATE TABLE transactions (
    id                   BINARY(16)     NOT NULL,
    reference_number     VARCHAR(255)   NOT NULL,
    sender_account_id    BINARY(16)     NOT NULL,
    receiver_account_id  BINARY(16)     NOT NULL,
    amount               DECIMAL(19, 4) NOT NULL,
    transaction_type     VARCHAR(255)   NOT NULL,
    status               VARCHAR(255)   NOT NULL,
    description          VARCHAR(255)   NULL,
    created_at           DATETIME(6)    NOT NULL,
    updated_at           DATETIME(6)    NULL,
    version              BIGINT         NULL,

    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT uk_transactions_reference_number UNIQUE (reference_number),
    CONSTRAINT fk_transactions_sender FOREIGN KEY (sender_account_id) REFERENCES accounts (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_receiver FOREIGN KEY (receiver_account_id) REFERENCES accounts (id) ON DELETE RESTRICT,
    CONSTRAINT chk_transactions_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_transactions_sender ON transactions (sender_account_id);
CREATE INDEX idx_transactions_receiver ON transactions (receiver_account_id);
CREATE INDEX idx_transactions_reference_number ON transactions (reference_number);
CREATE INDEX idx_transactions_created_at ON transactions (created_at);
