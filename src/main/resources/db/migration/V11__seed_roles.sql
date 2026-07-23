-- ============================================================
-- V11: Seed default roles
-- ============================================================

INSERT IGNORE INTO roles (id, name, description, created_at, version)
VALUES
    (UNHEX(REPLACE('a1b2c3d4-e5f6-7890-abcd-ef1234567001', '-', '')),
     'ROLE_ADMIN', 'System administrator with full access',
     NOW(6), 0),

    (UNHEX(REPLACE('a1b2c3d4-e5f6-7890-abcd-ef1234567002', '-', '')),
     'ROLE_EMPLOYEE', 'Bank employee with operational access',
     NOW(6), 0),

    (UNHEX(REPLACE('a1b2c3d4-e5f6-7890-abcd-ef1234567003', '-', '')),
     'ROLE_CUSTOMER', 'Bank customer with limited access',
     NOW(6), 0);
