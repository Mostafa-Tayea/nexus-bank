# NEXUS BANK — Complete API Testing Guide (Postman)

## Table of Contents

1. [Environment Setup](#1-environment-setup)
2. [Postman Environment Variables](#2-postman-environment)
3. [API Testing Order](#3-api-testing-order)
4. [Endpoint Documentation (All 41 Endpoints)](#4-endpoint-documentation)
5. [Authentication Flow](#5-authentication-flow)
6. [Account Flow](#6-account-flow)
7. [Transaction Flow](#7-transaction-flow)
8. [Notification Flow](#8-notification-flow)
9. [Audit Flow](#9-audit-flow)
10. [JWT Testing](#10-jwt-testing)
11. [Validation Testing (Negative Cases)](#11-validation-testing)
12. [Security Testing](#12-security-testing)
13. [Database Verification](#13-database-verification)
14. [Redis Verification](#14-redis-verification)
15. [Email Verification](#15-email-verification)
16. [Postman Automation Scripts](#16-postman-automation)
17. [Collection Runner](#17-collection-runner)
18. [Final Acceptance Checklist](#18-final-acceptance-checklist)

---

## 1. Environment Setup

### 1.1 Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Java (JDK) | 21+ | Spring Boot runtime |
| MySQL | 8.0+ | Primary database |
| Redis | 7.0+ | Caching, JWT blacklist, login attempts (optional — falls back to in-memory) |
| SMTP Server | any (MailHog/Mailtrap recommended for dev) | Email notifications |
| Maven | 3.9+ | Build tool |
| Postman | Latest | API testing |

### 1.2 Database Setup

```sql
CREATE DATABASE test;
-- Flyway handles all table creation (V1–V10) and seed data (V11)
```

The application connects to `localhost:3306/test` with user `root`/`root` by default. Override with environment variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.

### 1.3 Run the Application

```bash
# From project root
./mvnw spring-boot:run
# OR
mvn spring-boot:run
```

The application starts on **port 8080** by default.

### 1.4 Verify Services

| Check | URL | Expected |
|-------|-----|----------|
| Health | `GET http://localhost:8080/actuator/health` | `{"status":"UP"}` |
| Swagger UI | `GET http://localhost:8080/swagger-ui.html` | HTML page |
| OpenAPI Docs | `GET http://localhost:8080/v3/api-docs` | JSON OpenAPI spec |

### 1.5 Flyway Migrations

11 migrations run automatically on startup:

| Migration | Table | Purpose |
|-----------|-------|---------|
| V1 | `roles` | Role definitions |
| V2 | `users` | User accounts |
| V3 | `user_roles` | User-Role join table |
| V4 | `accounts` | Bank accounts |
| V5 | `transactions` | Transaction records |
| V6 | `refresh_tokens` | JWT refresh tokens |
| V7 | `verification_tokens` | Email verification tokens |
| V8 | `otp_codes` | OTP codes |
| V9 | `notifications` | User notifications |
| V10 | `audit_logs` | Audit trail |
| V11 | roles (seed) | ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_CUSTOMER |

---

## 2. Postman Environment Variables

### 2.1 Collection Variables

Create a Postman Environment called **Nexus Bank** with these variables:

| Variable | Initial Value | Purpose |
|----------|---------------|---------|
| `baseUrl` | `http://localhost:8080` | Base API URL |
| `accessToken` | *(empty — set after login)* | JWT Bearer token |
| `refreshToken` | *(empty — set after login)* | Refresh token for rotation |
| `userId` | *(empty — set after register/login)* | Current user UUID |
| `adminAccessToken` | *(empty — set after admin login)* | Admin JWT token |
| `adminRefreshToken` | *(empty — set after admin login)* | Admin refresh token |
| `adminUserId` | *(empty — set after admin login)* | Admin user UUID |
| `employeeAccessToken` | *(empty — set after employee login)* | Employee JWT token |
| `employeeRefreshToken` | *(empty — set after employee login)* | Employee refresh token |
| `accountId` | *(empty — set after account creation)* | Bank account UUID |
| `accountNumber` | *(empty — set after account creation)* | 10-digit account number |
| `transactionId` | *(empty — set after transaction)* | Transaction UUID |
| `referenceNumber` | *(empty — set after transaction)* | TXN reference number |
| `notificationId` | *(empty — set after notification fetch)* | Notification UUID |
| `verificationToken` | *(empty — set after registration)* | Email verification token |
| `otp` | *(empty — set after OTP generation)* | 6-digit OTP code |
| `email` | `test@example.com` | Test user email |
| `password` | `P@ssw0rd123` | Test user password |
| `adminEmail` | `admin@nexusbank.com` | Admin email |
| `adminPassword` | `Admin@123` | Admin password |

### 2.2 Automatic Token Storage — Postman Tests Snippets

**For Login request — Tests tab:**

```javascript
var response = pm.response.json();
if (response.success) {
    pm.environment.set("accessToken", response.data.accessToken);
    pm.environment.set("refreshToken", response.data.refreshToken);
    pm.environment.set("userId", response.data.user.id);
}
```

**For Register request — Tests tab:**

```javascript
var response = pm.response.json();
if (response.success && response.data.token) {
    pm.environment.set("verificationToken", response.data.token);
}
```

**For Admin Login — Tests tab:**

```javascript
var response = pm.response.json();
if (response.success) {
    pm.environment.set("adminAccessToken", response.data.accessToken);
    pm.environment.set("adminRefreshToken", response.data.refreshToken);
    pm.environment.set("adminUserId", response.data.user.id);
}
```

**For Account Creation — Tests tab:**

```javascript
var response = pm.response.json();
if (response.success) {
    pm.environment.set("accountId", response.data.id);
    pm.environment.set("accountNumber", response.data.accountNumber);
}
```

**For Any Transaction — Tests tab:**

```javascript
var response = pm.response.json();
if (response.success) {
    pm.environment.set("transactionId", response.data.id);
    pm.environment.set("referenceNumber", response.data.referenceNumber);
}
```

**For Notification Fetch — Tests tab:**

```javascript
var response = pm.response.json();
if (response.success && response.data.content && response.data.content.length > 0) {
    pm.environment.set("notificationId", response.data.content[0].id);
}
```

**For Refresh Token — Tests tab:**

```javascript
var response = pm.response.json();
if (response.success) {
    pm.environment.set("accessToken", response.data.accessToken);
    pm.environment.set("refreshToken", response.data.refreshToken);
}
```

**For Forgot Password — Tests tab:**

```javascript
// OTP is sent via email — check SMTP logs or database
// Manually set: pm.environment.set("otp", "123456");
```

---

## 3. API Testing Order

Execute requests in this exact sequence. Each step depends on the previous.

### Phase 1: Bootstrap (Admin Setup)

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 1 | `POST /api/v1/auth/register` | Register Admin user | None |
| 2 | `POST /api/v1/auth/verify-email` | Verify admin email | Step 1 |
| 3 | `POST /api/v1/auth/login` | Login as admin | Step 2 |
| 4 | Save admin tokens | Auto-save via Tests script | Step 3 |

> **Note:** The first registered user gets ROLE_CUSTOMER by default. Admin/Employee roles must be assigned directly in the database or via an admin user management endpoint. For testing purposes, insert admin via SQL:
> ```sql
> INSERT INTO user_roles (user_id, role_id)
> SELECT u.id, r.id FROM users u, roles r
> WHERE u.email = 'admin@nexusbank.com' AND r.name = 'ROLE_ADMIN';
> ```

### Phase 2: Customer Registration & Authentication

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 5 | `POST /api/v1/auth/register` | Register Customer | None |
| 6 | `POST /api/v1/auth/verify-email` | Verify customer email | Step 5 |
| 7 | `POST /api/v1/auth/login` | Login as customer | Step 6 |
| 8 | Save customer tokens | Auto-save via Tests script | Step 7 |

### Phase 3: Account Management

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 9 | `POST /api/v1/accounts` | Create Account (ADMIN) | Step 4 (admin token) |
| 10 | `GET /api/v1/accounts/{id}` | Get Account by ID | Step 9 |
| 11 | `GET /api/v1/accounts/number/{accountNumber}` | Get by Account Number | Step 9 |
| 12 | `GET /api/v1/accounts/user/{userId}` | Get User's Accounts | Step 9 |
| 13 | `GET /api/v1/accounts/my-accounts` | Get My Accounts | Step 8 (customer token) |

### Phase 4: Transactions

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 14 | `POST /api/v1/transactions/deposit` | Deposit Funds | Step 9 (admin token) |
| 15 | Verify balance via `GET /api/v1/accounts/{id}` | Balance check | Step 9 |
| 16 | `POST /api/v1/transactions/withdraw` | Withdraw Funds | Step 9 (admin token) |
| 17 | Verify balance via `GET /api/v1/accounts/{id}` | Balance check | Step 9 |
| 18 | Create 2nd Account | For transfer testing | Step 9 |
| 19 | Deposit to 2nd Account | Seed for transfer | Step 18 |
| 20 | `POST /api/v1/transactions/transfer` | Transfer Between Accounts | Step 8 (customer token) |
| 21 | Verify both balances | Balance check | Steps 9, 18 |
| 22 | `POST /api/v1/transactions/{id}/reverse` | Reverse a Transaction | Step 9 (admin token) |
| 23 | `GET /api/v1/transactions/{id}` | Get Transaction by ID | Step 14 |
| 24 | `GET /api/v1/transactions/reference/{ref}` | Get by Reference Number | Step 14 |
| 25 | `GET /api/v1/transactions` | Transaction History (ADMIN) | Step 4 |
| 26 | `GET /api/v1/transactions/my-transactions` | My Transactions (CUSTOMER) | Step 8 |

### Phase 5: Notifications

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 27 | `GET /api/v1/notifications` | Get All Notifications | Step 8 |
| 28 | `GET /api/v1/notifications/unread` | Get Unread Notifications | Step 8 |
| 29 | `PATCH /api/v1/notifications/{id}/read` | Mark As Read | Step 27 |
| 30 | `PATCH /api/v1/notifications/read-all` | Mark All As Read | Step 8 |
| 31 | `DELETE /api/v1/notifications/{id}` | Delete Notification | Step 27 |

### Phase 6: User Management (ADMIN)

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 32 | `GET /api/v1/users/profile` | Get Own Profile | Step 8 |
| 33 | `PUT /api/v1/users/profile` | Update Profile | Step 8 |
| 34 | `PUT /api/v1/users/change-password` | Change Password | Step 8 |
| 35 | `GET /api/v1/users/{id}` | Get User by ID (ADMIN) | Step 4 |
| 36 | `GET /api/v1/users` | Get All Users (ADMIN) | Step 4 |
| 37 | `PATCH /api/v1/users/{id}/lock` | Lock User (ADMIN) | Step 4 |
| 38 | `PATCH /api/v1/users/{id}/unlock` | Unlock User (ADMIN) | Step 4 |
| 39 | `PATCH /api/v1/users/{id}/enable` | Enable User (ADMIN) | Step 4 |
| 40 | `PATCH /api/v1/users/{id}/disable` | Disable User (ADMIN) | Step 4 |

### Phase 7: Audit Logs (ADMIN)

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 41 | `GET /api/v1/audit` | Get All Audit Logs | Step 4 |
| 42 | `GET /api/v1/audit/user/{userId}` | Get Audit By User | Step 4 |
| 43 | `GET /api/v1/audit/event/{eventType}` | Get Audit By Event Type | Step 4 |

### Phase 8: Token Lifecycle

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 44 | `POST /api/v1/auth/refresh-token` | Refresh Access Token | Step 8 |
| 45 | `POST /api/v1/auth/logout` | Logout | Step 8 |
| 46 | Attempt request with blacklisted token | Verify JWT blacklist | Step 45 |

### Phase 9: Password Reset Flow

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 47 | `POST /api/v1/auth/forgot-password` | Request Password Reset | Step 7 (email) |
| 48 | `POST /api/v1/auth/verify-otp` | Verify OTP Code | Step 47 |
| 49 | `POST /api/v1/auth/reset-password` | Reset Password | Step 48 |
| 50 | `POST /api/v1/auth/login` | Login with New Password | Step 49 |

### Phase 10: Account Lifecycle (Freeze/Close)

| Step | Request | Description | Dependencies |
|------|---------|-------------|--------------|
| 51 | `PATCH /api/v1/accounts/{id}/freeze` | Freeze Account | Step 4 |
| 52 | Attempt deposit on frozen account | Should fail | Step 51 |
| 53 | `PATCH /api/v1/accounts/{id}/close` | Close Account | Step 4 |

### Phase 11: Negative Testing

| Step | Request | Description |
|------|---------|-------------|
| 54 | All negative test cases | See Section 11 |

---

## 4. Endpoint Documentation

### 4.1 Authentication Endpoints (`/api/v1/auth`)

---

#### `POST /api/v1/auth/register`

**Purpose:** Register a new customer account

**Headers:**
```
Content-Type: application/json
```

**Authorization:** None (public endpoint)

**Request Body:**
```json
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+201012345678",
    "nationalId": "29001011234567",
    "password": "P@ssw0rd123",
    "confirmPassword": "P@ssw0rd123"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| `firstName` | Required, max 50 chars |
| `lastName` | Required, max 50 chars |
| `email` | Required, valid email, max 100 chars, unique |
| `phone` | Required, Egyptian format: `+20XXXXXXXXXX` or `01XXXXXXXXX`, unique |
| `nationalId` | Required, exactly 14 digits, unique |
| `password` | Required, min 8 chars, must contain: uppercase, lowercase, digit, special char (`@$!%*?&#^()-_=+`), BCrypt(12) |
| `confirmPassword` | Required, must match `password` |

**Expected Response (201):**
```json
{
    "success": true,
    "message": "Registration successful",
    "data": {
        "message": "Registration successful. Please verify your email.",
        "token": "550e8400-e29b-41d4-a716-446655440000"
    },
    "timestamp": "2026-07-25T10:00:00",
    "path": "/api/v1/auth/register"
}
```

**Possible Errors:**
| Status | Condition |
|--------|-----------|
| 400 | Validation failed (missing fields, weak password, invalid phone/nationalId) |
| 400 | Passwords do not match |
| 409 | Duplicate email, phone, or national ID |
| 429 | Rate limit exceeded (10 req/60s) |

**Events Triggered:** `UserRegisteredEvent`, `OtpGeneratedEvent`

**Database Changes:**
| Table | Change |
|-------|--------|
| `users` | New row (enabled=false, failedAttempts=0) |
| `user_roles` | New row linking user to ROLE_CUSTOMER |
| `verification_tokens` | New row (UUID token, 24h expiry) |
| `otp_codes` | New row (6-digit code, 10min expiry, EMAIL_VERIFICATION purpose) |

**Notifications Triggered:** Welcome IN_APP notification, OTP EMAIL notification

**Audit Record:** Created via `AuditListener` with event_type=`USER_REGISTERED`

---

#### `POST /api/v1/auth/verify-email`

**Purpose:** Verify user's email using the token from registration

**Headers:**
```
Content-Type: application/json
```

**Authorization:** None

**Request Body:**
```json
{
    "token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| `token` | Required, must be valid unused token, not expired (24h) |

**Expected Response (200):**
```json
{
    "success": true,
    "message": "Email verified successfully",
    "data": {
        "message": "Email verified successfully",
        "token": null
    },
    "timestamp": "2026-07-25T10:05:00",
    "path": "/api/v1/auth/verify-email"
}
```

**Possible Errors:**
| Status | Condition |
|--------|-----------|
| 400 | Invalid or already-used verification token |
| 400 | Verification token expired (>24h) |
| 429 | Rate limit exceeded (5 req/60s) |

**Events Triggered:** `EmailVerifiedEvent`

**Database Changes:**
| Table | Change |
|-------|--------|
| `users` | `enabled` to `true` |
| `verification_tokens` | `verified` to `true`, `verified_at` set |

**Notifications Triggered:** Email Verified IN_APP notification

---

#### `POST /api/v1/auth/login`

**Purpose:** Authenticate user and get JWT tokens

**Headers:**
```
Content-Type: application/json
```

**Authorization:** None

**Request Body:**
```json
{
    "email": "john.doe@example.com",
    "password": "P@ssw0rd123"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| `email` | Required, valid email |
| `password` | Required |

**Expected Response (200):**
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 900,
        "user": {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "firstName": "John",
            "lastName": "Doe",
            "email": "john.doe@example.com",
            "phone": "+201012345678",
            "enabled": true,
            "createdAt": "2026-07-25T10:00:00"
        }
    },
    "timestamp": "2026-07-25T10:10:00",
    "path": "/api/v1/auth/login"
}
```

**Possible Errors:**
| Status | Condition |
|--------|-----------|
| 400 | Invalid email or password (BadCredentialsException) |
| 401 | Invalid email or password |
| 400 | Account not verified (DisabledException) |
| 400 | Account locked due to failed attempts (LockedException) |
| 429 | Rate limit exceeded (5 req/60s) |

**Business Rules:**
- User must be `enabled=true` (email verified)
- User must be `accountNonLocked=true`
- Failed attempts increment; after 5 failures, account locks for 15 minutes
- Successful login resets `failedAttempts` to 0, updates `lastLogin`
- Access token expires in 15 minutes (900,000 ms)
- Refresh token expires in 7 days (604,800,000 ms)

**Events Triggered:** `UserLoggedInEvent`

**Database Changes:**
| Table | Change |
|-------|--------|
| `users` | `failedAttempts` to 0, `lastLogin` updated |
| `refresh_tokens` | New row (JWT refresh token, 7-day expiry) |

**Notifications Triggered:** Login Successful IN_APP notification

---

#### `POST /api/v1/auth/refresh-token`

**Purpose:** Get new access + refresh token pair using valid refresh token (rotation)

**Headers:**
```
Content-Type: application/json
```

**Authorization:** None

**Request Body:**
```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Expected Response (200):**
```json
{
    "success": true,
    "message": "Token refreshed successfully",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...(new)",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...(new)",
        "tokenType": "Bearer",
        "expiresIn": 900
    }
}
```

**Business Rules:**
- Old refresh token is revoked (rotation pattern)
- New access + refresh pair issued
- User is extracted from old refresh token

**Possible Errors:**
| Status | Condition |
|--------|-----------|
| 400 | Refresh token not found |
| 400 | Refresh token already revoked |
| 400 | Refresh token expired |

---

#### `POST /api/v1/auth/logout`

**Purpose:** Logout — blacklist access token and revoke refresh token

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <accessToken>
```

**Request Body:**
```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Business Rules:**
- Access token is blacklisted in Redis (TTL matches token expiry)
- Refresh token is revoked in DB
- Login attempts cleared from Redis cache

**Database Changes:**
| Table | Change |
|-------|--------|
| `refresh_tokens` | `revoked` to `true` |

**Redis Changes:**
| Key | Change |
|-----|--------|
| `jwt:blacklist:<token>` | Created with TTL matching access token expiry |
| `login:attempt:<email>` | Deleted |

---

#### `POST /api/v1/auth/forgot-password`

**Purpose:** Request password reset OTP

**Headers:**
```
Content-Type: application/json
```

**Authorization:** None

**Request Body:**
```json
{
    "email": "john.doe@example.com"
}
```

**Business Rules:**
- Always returns success message (prevents email enumeration)
- If email exists, generates 6-digit OTP (10-min expiry)
- Previous unverified OTPs for this purpose are deleted

**Events Triggered:** `OtpGeneratedEvent`

**Database Changes:**
| Table | Change |
|-------|--------|
| `otp_codes` | Old RESET_PASSWORD OTPs deleted, new row created |

**Rate Limit:** 3 requests per 300 seconds

---

#### `POST /api/v1/auth/verify-otp`

**Purpose:** Verify an OTP code

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
    "code": "123456",
    "purpose": "RESET_PASSWORD"
}
```

**Valid `purpose` values:** `EMAIL_VERIFICATION`, `LOGIN`, `TRANSFER`, `RESET_PASSWORD`

**Business Rules:**
- OTP must exist and be unverified
- OTP must not be expired (10 minutes)
- Code must match the purpose

---

#### `POST /api/v1/auth/reset-password`

**Purpose:** Reset password using OTP code

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
    "token": "123456",
    "newPassword": "NewP@ssw0rd123",
    "confirmPassword": "NewP@ssw0rd123"
}
```

**Business Rules:**
- `token` here is actually the OTP code (not a UUID)
- Password must pass `@ValidPassword` validation
- Passwords must match
- All user's refresh tokens are revoked (forces re-login)

**Events Triggered:** `PasswordResetEvent`

**Database Changes:**
| Table | Change |
|-------|--------|
| `users` | `password` to new BCrypt hash |
| `otp_codes` | `verified` to `true` |
| `refresh_tokens` | All tokens for this user to `revoked=true` |

---

### 4.2 User Endpoints (`/api/v1/users`)

---

#### `GET /api/v1/users/profile`

**Purpose:** Get current user's full profile

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Authorization:** Any authenticated user

**Expected Response (200):**
```json
{
    "success": true,
    "message": "Profile fetched successfully",
    "data": {
        "id": "550e8400-...",
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "phone": "+201012345678",
        "nationalId": "29001011234567",
        "enabled": true,
        "accountNonLocked": true,
        "roles": ["ROLE_CUSTOMER"],
        "lastLogin": "2026-07-25T10:10:00",
        "createdAt": "2026-07-25T10:00:00"
    }
}
```

**Caching:** `@Cacheable(value="users", key="#email")` — cached for 3600s

---

#### `PUT /api/v1/users/profile`

**Purpose:** Update current user's profile

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <accessToken>
```

**Request Body:**
```json
{
    "firstName": "John Updated",
    "lastName": "Doe Jr",
    "phone": "+201234567890"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| `firstName` | Optional, max 50 chars |
| `lastName` | Optional, max 50 chars |
| `phone` | Optional, Egyptian phone format, must be unique |

**Caching:** `@CachePut(value="users", key="#email")` — updates cache

---

#### `PUT /api/v1/users/change-password`

**Purpose:** Change current user's password

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <accessToken>
```

**Request Body:**
```json
{
    "currentPassword": "P@ssw0rd123",
    "newPassword": "NewP@ssw0rd456",
    "confirmPassword": "NewP@ssw0rd456"
}
```

**Business Rules:**
- Current password must be correct
- New password must pass `@ValidPassword` validation
- Passwords must match
- Cache evicted for user

**Events Triggered:** `PasswordChangedEvent`

---

#### `GET /api/v1/users/{id}` — ADMIN only

**Headers:**
```
Authorization: Bearer <adminAccessToken>
```

**PreAuthorize:** `hasRole('ADMIN')`

---

#### `GET /api/v1/users` — ADMIN only

**Query Parameters:**
| Param | Default | Description |
|-------|---------|-------------|
| `search` | — | Search by name or email |
| `page` | 0 | Page number (0-indexed) |
| `size` | 10 | Page size |
| `sortBy` | `createdAt` | Sort field |
| `sortDir` | `desc` | Sort direction |

---

#### `DELETE /api/v1/users/{id}` — ADMIN only

**PreAuthorize:** `hasRole('ADMIN')`

---

#### `PATCH /api/v1/users/{id}/lock` — ADMIN only

**PreAuthorize:** `hasRole('ADMIN')`

Sets `accountNonLocked = false`. Fires `UserLockedEvent`.

---

#### `PATCH /api/v1/users/{id}/unlock` — ADMIN only

Resets `accountNonLocked = true` and `failedAttempts = 0`.

---

#### `PATCH /api/v1/users/{id}/enable` — ADMIN only

Sets `enabled = true`.

---

#### `PATCH /api/v1/users/{id}/disable` — ADMIN only

Sets `enabled = false`.

---

### 4.3 Account Endpoints (`/api/v1/accounts`)

---

#### `POST /api/v1/accounts` — ADMIN/EMPLOYEE only

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <adminAccessToken>
```

**PreAuthorize:** `hasAnyRole('ADMIN', 'EMPLOYEE')`

**Query Parameter:** `userId` (UUID, required) — the user this account belongs to

**Request Body:**
```json
{
    "type": "SAVINGS",
    "currency": "USD",
    "dailyTransferLimit": 5000.00,
    "iban": "SA0380000000608010167519"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| `type` | Required, enum: `SAVINGS` or `CURRENT` |
| `currency` | Required, exactly 3 characters (ISO 4217) |
| `dailyTransferLimit` | Must be > 0 |
| `iban` | Required, valid IBAN format |

**Business Rules:**
- Account number auto-generated (10-digit unique)
- Balance starts at 0
- Status starts as `ACTIVE`
- IBAN must be unique

**Events Triggered:** `AccountCreatedEvent`

**Notifications Triggered:** Account Created IN_APP notification (via NotificationListener)

---

#### `GET /api/v1/accounts/{id}` — ADMIN/EMPLOYEE only
#### `GET /api/v1/accounts/number/{accountNumber}` — ADMIN/EMPLOYEE only
#### `GET /api/v1/accounts/user/{userId}` — ADMIN/EMPLOYEE or owner

**PreAuthorize (userId endpoint):** `hasAnyRole('ADMIN', 'EMPLOYEE') or #userId == authentication.principal.user.id`

This SpEL expression works because the JWT filter sets `CustomUserDetails` as the principal, and `CustomUserDetails.getUser().getId()` returns the user's UUID.

---

#### `GET /api/v1/accounts/my-accounts` — Any authenticated user

Returns all accounts belonging to the current user.

---

#### `PATCH /api/v1/accounts/{id}/freeze` — ADMIN only

**Query Parameter:** `reason` (optional string)

Sets `status = FROZEN`. Fires `AccountFrozenEvent`.

**Notifications:** Email notification sent about account freeze.

---

#### `PATCH /api/v1/accounts/{id}/close` — ADMIN only

**Query Parameter:** `reason` (optional string)

Sets `status = CLOSED`. Fires `AccountClosedEvent`.

**Notifications:** Email notification sent about account closure.

---

### 4.4 Transaction Endpoints (`/api/v1/transactions`)

---

#### `POST /api/v1/transactions/deposit` — ADMIN/TELLER only

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <adminAccessToken>
```

**PreAuthorize:** `hasAnyRole('ADMIN', 'TELLER')`

**Request Body:**
```json
{
    "accountNumber": "1234567890",
    "amount": 1000.00
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| `accountNumber` | Required |
| `amount` | Required, min 0.01 |

**Business Rules:**
- Account must exist and be `ACTIVE`
- Balance increases by deposit amount
- Pessimistic locking via `findByAccountNumberForUpdate`
- Reference number format: `TXN<timestamp><8-char UUID>`
- Transaction status: `SUCCESS`

**Balance Change:** `newBalance = oldBalance + amount`

**Events Triggered:** `MoneyDepositedEvent`, `TransactionCompletedEvent`

**Notifications:** IN_APP notification for deposit

---

#### `POST /api/v1/transactions/withdraw` — ADMIN/TELLER only

**Request Body:**
```json
{
    "accountNumber": "1234567890",
    "amount": 500.00
}
```

**Business Rules:**
- Account must exist and be `ACTIVE`
- Balance must be >= withdrawal amount
- Pessimistic locking

**Balance Change:** `newBalance = oldBalance - amount`

**Possible Errors:**
| Status | Condition |
|--------|-----------|
| 400 | Insufficient balance |
| 400 | Account not active |

---

#### `POST /api/v1/transactions/transfer` — ADMIN/TELLER/CUSTOMER

**PreAuthorize:** `hasAnyRole('ADMIN', 'TELLER', 'CUSTOMER')`

**Request Body:**
```json
{
    "senderAccountNumber": "1234567890",
    "receiverAccountNumber": "0987654321",
    "amount": 200.00,
    "description": "Monthly rent payment"
}
```

**Validation Rules:**
| Field | Rules |
|-------|-------|
| `senderAccountNumber` | Required |
| `receiverAccountNumber` | Required |
| `amount` | Required, min 0.01 |
| `description` | Optional, max 255 chars |

**Business Rules:**
- Cannot transfer to same account
- Both accounts must be `ACTIVE`
- Source balance must be >= transfer amount
- Daily transfer limit check: sum of today's transfers + this amount must not exceed `dailyTransferLimit`
- `dailyTransferredAmount` on source account is incremented

**Balance Changes:**
```
senderBalance = senderBalance - amount
receiverBalance = receiverBalance + amount
```

**Events Triggered:** `MoneyTransferredEvent`, `TransactionCompletedEvent`

**Notifications:** Both sender and receiver get IN_APP notifications

---

#### `POST /api/v1/transactions/{id}/reverse` — ADMIN only

**Query Parameter:** `reason` (optional string)

**Business Rules:**
- Only `SUCCESS` transactions can be reversed
- Already `REVERSED` transactions cannot be reversed again

**Reversal Logic by Transaction Type:**
| Type | Balance Effect |
|------|---------------|
| DEPOSIT | Receiver balance -= amount |
| WITHDRAW | Sender balance += amount |
| TRANSFER | Sender += amount, Receiver -= amount, dailyTransferredAmount -= amount |

A new `REVERSAL` transaction record is created. Original transaction status to `REVERSED`.

---

#### `GET /api/v1/transactions/{id}` — ADMIN/TELLER only
#### `GET /api/v1/transactions/reference/{referenceNumber}` — ADMIN/TELLER only
#### `GET /api/v1/transactions` — ADMIN/TELLER only

**Query Parameters (for history):**
| Param | Default | Description |
|-------|---------|-------------|
| `search` | — | Search by reference number or description |
| `transactionType` | — | Filter: DEPOSIT, WITHDRAW, TRANSFER, REVERSAL |
| `status` | — | Filter: PENDING, SUCCESS, FAILED, REVERSED |
| `minAmount` | — | Minimum amount filter |
| `maxAmount` | — | Maximum amount filter |
| `fromDate` | — | ISO 8601 DateTime |
| `toDate` | — | ISO 8601 DateTime |
| `accountId` | — | Filter by account UUID |
| `page` | 0 | Page number |
| `size` | 10 | Page size |
| `sortBy` | `createdAt` | Sort field |
| `sortDir` | `desc` | Sort direction |

---

#### `GET /api/v1/transactions/my-transactions` — CUSTOMER only

Same query parameters as above except `accountId` (derived from authenticated user's accounts).

---

### 4.5 Notification Endpoints (`/api/v1/notifications`)

---

#### `GET /api/v1/notifications` — ADMIN/CUSTOMER/EMPLOYEE

Returns paginated notifications for the current user with unread count.

**Query Parameters:** `page`, `size`, `sortBy`, `sortDir`

**Response includes:** `unreadCount` field

**Caching:** `@Cacheable(value="notificationCount", key="#userId")` on unread count

---

#### `GET /api/v1/notifications/{id}` — ADMIN/CUSTOMER/EMPLOYEE

Returns a single notification. **Ownership check:** only the notification's owner can access it.

---

#### `GET /api/v1/notifications/unread` — ADMIN/CUSTOMER/EMPLOYEE

Returns only unread notifications.

---

#### `PATCH /api/v1/notifications/{id}/read` — ADMIN/CUSTOMER/EMPLOYEE

Marks a notification as read. Sets `status = READ`, `readAt = now()`.

**Cache:** Evicts `notificationCount` cache.

---

#### `PATCH /api/v1/notifications/read-all` — ADMIN/CUSTOMER/EMPLOYEE

Marks all user's notifications as read.

---

#### `DELETE /api/v1/notifications/{id}` — ADMIN/CUSTOMER/EMPLOYEE

Deletes a notification. Ownership check enforced.

---

### 4.6 Audit Endpoints (`/api/v1/audit`) — ADMIN only

All endpoints require `hasRole('ADMIN')` (class-level `@PreAuthorize`).

---

#### `GET /api/v1/audit`

**Query Parameters:**
| Param | Default | Description |
|-------|---------|-------------|
| `page` | 0 | Page number |
| `size` | 20 | Page size |
| `sortBy` | `timestamp` | Sort field |
| `sortDir` | `desc` | Sort direction |

---

#### `GET /api/v1/audit/user/{userId}`

---

#### `GET /api/v1/audit/event/{eventType}`

**Event types:** USER_REGISTERED, USER_LOGGED_IN, USER_LOCKED, PASSWORD_CHANGED, PASSWORD_RESET, EMAIL_VERIFIED, OTP_GENERATED, ACCOUNT_CREATED, ACCOUNT_FROZEN, ACCOUNT_CLOSED, MONEY_DEPOSITED, MONEY_WITHDRAWN, MONEY_TRANSFERRED

---

## 5. Authentication Flow

```
Register → Email Verification Token Generated → OTP Generated
    ↓
Verify Email (token) → User enabled=true
    ↓
Login (email + password) → JWT Access Token + Refresh Token
    ↓
Use Bearer token for authenticated requests
    ↓
After 15 min: Use Refresh Token to get new pair
    ↓
Logout: Blacklist access token + revoke refresh token
```

### Password Reset Flow

```
Forgot Password (email) → 6-digit OTP sent
    ↓
Verify OTP (code + purpose=RESET_PASSWORD)
    ↓
Reset Password (OTP code as token + new password)
    ↓
All refresh tokens revoked → Must re-login
```

---

## 6. Account Flow

```
Admin/Employee creates account for user
    ↓
Account starts ACTIVE with balance=0
    ↓
Admin/Employee deposits funds → balance increases
    ↓
Admin/Employee withdraws funds → balance decreases (must have sufficient)
    ↓
Customer transfers between own accounts or to others
    ↓
Admin can freeze → no more transactions allowed
    ↓
Admin can close → no more transactions allowed
```

**Account Status Flow:**
```
ACTIVE → FROZEN → ACTIVE (no unfreeze endpoint exists — only freeze/close)
ACTIVE → CLOSED (terminal)
```

---

## 7. Transaction Flow

### Balance Tracking

| After Request | Account A | Account B |
|---------------|-----------|-----------|
| Initial | $0.00 | $0.00 |
| Deposit $1000 to A | $1,000.00 | $0.00 |
| Deposit $500 to B | $1,000.00 | $500.00 |
| A transfers $200 to B | $800.00 | $700.00 |
| A withdraws $100 | $700.00 | $700.00 |
| Reverse the transfer | $900.00 | $500.00 |
| Reverse the withdrawal | $800.00 | $500.00 |

### Reference Number Format

`TXN` + epoch millis + 8 uppercase hex chars from UUID

Example: `TXN1690000000000A1B2C3D4`

### Daily Transfer Limit Reset

A scheduler (`DailyTransferLimitResetJob`) runs at midnight daily and resets all accounts' `dailyTransferredAmount` to 0.

---

## 8. Notification Flow

Notifications are generated asynchronously via `@Async @EventListener` in `NotificationListener`. Each business event creates an in-app notification.

| Event | Notification Title | Type |
|-------|-------------------|------|
| UserRegistered | Welcome to Nexus Bank | IN_APP |
| UserLoggedIn | Login Successful | IN_APP |
| PasswordChanged | Password Changed | EMAIL |
| PasswordReset | Password Reset | EMAIL |
| EmailVerified | Email Verified | IN_APP |
| MoneyDeposited | Money Deposited | IN_APP |
| MoneyWithdrawn | Money Withdrawn | IN_APP |
| MoneyTransferred (sender) | Transfer Sent | IN_APP |
| MoneyTransferred (receiver) | Transfer Received | IN_APP |
| TransactionFailed | Transaction Failed | IN_APP |
| AccountFrozen | Account Frozen | EMAIL |
| AccountClosed | Account Closed | EMAIL |
| OtpGenerated | Verification Code | EMAIL |

**Notification Lifecycle:**
```
Created (UNREAD) → Sent (SENT) → Read (READ)
                     ↓ (on error)
                  Failed (FAILED)
```

**Database:** `notifications` table, `status` column tracks state, `readAt` set on read.

---

## 9. Audit Flow

Audit logs are generated asynchronously via `@Async @EventListener` in `AuditListener`. Every significant business action creates an audit record.

**Fields captured:**
| Field | Source |
|-------|--------|
| `eventType` | Event class name |
| `action` | Derived from event type |
| `username` | User email |
| `userId` | User UUID |
| `ipAddress` | From request context |
| `httpMethod` | HTTP method |
| `endpoint` | Request URI |
| `result` | SUCCESS/FAILURE |
| `details` | JSON details |
| `timestamp` | LocalDateTime.now() |

**Scheduler:** `OldAuditCleanupJob` deletes audit logs older than 365 days (runs at 4 AM daily).

---

## 10. JWT Testing

### 10.1 Expired Token

**Setup:** Wait 15+ minutes after login (or use a token with a past expiry).

**Request:** Any authenticated endpoint with expired Bearer token.

**Expected Response (401):**
```json
{
    "success": false,
    "message": "Authentication is required to access this resource",
    "timestamp": "...",
    "path": "/api/v1/users/profile"
}
```

### 10.2 Invalid/Malformed Token

**Request:** `Authorization: Bearer invalid.token.here`

**Expected:** 401 Unauthorized

### 10.3 Tampered Token

**Request:** Modify one character in a valid JWT.

**Expected:** 401 Unauthorized (JWT signature verification fails)

### 10.4 Missing Token

**Request:** Any protected endpoint without `Authorization` header.

**Expected:** 401 Unauthorized

### 10.5 Blacklisted Token (After Logout)

**Setup:** Login, then logout, then try using the same access token.

**Expected:** 401 Unauthorized — `JwtAuthenticationFilter` checks `JwtBlacklistService.isTokenBlacklisted()` and silently drops the request.

### 10.6 Role Authorization

**Setup:** Login as CUSTOMER, attempt ADMIN endpoints.

**Request:** `GET /api/v1/audit` with customer Bearer token.

**Expected Response (403):**
```json
{
    "success": false,
    "message": "You do not have permission to access this resource",
    "timestamp": "...",
    "path": "/api/v1/audit"
}
```

---

## 11. Validation Testing (Negative Cases)

### 11.1 Registration Validation

| Test Case | Request Body | Expected Status | Expected Error |
|-----------|-------------|----------------|----------------|
| Duplicate Email | Email already registered | 409 | "User already exists with email: ..." |
| Duplicate Phone | Phone already registered | 409 | "User already exists with phone: ..." |
| Duplicate National ID | ID already registered | 409 | "User already exists with national ID: ..." |
| Weak Password (no uppercase) | `"password": "weakpass1!"` | 400 | Validation error |
| Weak Password (no digit) | `"password": "WeakPass!@"` | 400 | Validation error |
| Weak Password (no special char) | `"password": "WeakPass12"` | 400 | Validation error |
| Weak Password (too short) | `"password": "Wp1!a"` | 400 | Validation error |
| Passwords Don't Match | `password != confirmPassword` | 400 | "Passwords do not match" |
| Invalid Phone Format | `"phone": "12345"` | 400 | Phone validation error |
| Invalid National ID | `"nationalId": "123"` | 400 | National ID must be 14 digits |
| Invalid Email | `"email": "notanemail"` | 400 | "Email must be valid" |
| Missing Required Fields | Any null/empty required field | 400 | Field-specific error |
| Empty Body | `{}` | 400 | Multiple validation errors |

### 11.2 Login Validation

| Test Case | Expected Status |
|-----------|----------------|
| Wrong Password | 400/401 |
| Non-existent Email | 400/401 |
| Unverified Account (enabled=false) | 400 |
| Locked Account (accountNonLocked=false) | 400 |
| After 5 Failed Attempts → Account Locked | 400 |

### 11.3 Transaction Validation

| Test Case | Expected Status | Expected Error |
|-----------|----------------|----------------|
| Negative Deposit Amount | 400 | Validation: min 0.01 |
| Deposit to Non-existent Account | 404 | Entity not found |
| Deposit to Frozen Account | 400 | "Account is not active" |
| Deposit to Closed Account | 400 | "Account is not active" |
| Withdraw More Than Balance | 400 | InsufficientBalanceException |
| Withdraw from Frozen Account | 400 | "Account is not active" |
| Transfer to Same Account | 400 | "Cannot transfer to the same account" |
| Transfer Exceeding Daily Limit | 400 | TransferLimitExceededException |
| Reverse Already Reversed Transaction | 400 | "Transaction is already reversed" |
| Reverse Failed Transaction | 400 | "Only successful transactions can be reversed" |
| Reverse Non-existent Transaction | 404 | Entity not found |
| Non-existent Reference Number | 404 | Entity not found |

### 11.4 Account Validation

| Test Case | Expected Status |
|-----------|----------------|
| Duplicate IBAN | 409 |
| Invalid IBAN | 400 |
| Non-existent User for Account | 404 |
| Create Account with Negative Limit | 400 |

### 11.5 Auth Flow Validation

| Test Case | Expected Status | Expected Error |
|-----------|----------------|----------------|
| Verify with Used Token | 400 | "Verification token is invalid or already used" |
| Verify with Expired Token (>24h) | 400 | "Verification token has expired" |
| Verify OTP with Wrong Code | 400 | "OTP code is invalid or already used" |
| Verify Expired OTP (>10 min) | 400 | "OTP code has expired" |
| Reset Password with Invalid OTP | 400 | "Reset token is invalid or already used" |
| Reset Password with Mismatched Passwords | 400 | "Passwords do not match" |
| Refresh with Revoked Token | 400 | "Refresh token has been revoked" |
| Refresh with Expired Token | 400 | "Refresh token has expired" |
| Refresh with Non-existent Token | 400 | "Refresh token not found" |
| Change Password with Wrong Current | 400 | "Current password is incorrect" |
| Update Profile with Duplicate Phone | 409 | "User already exists with phone: ..." |

### 11.6 Authorization Validation

| Test Case | Expected Status |
|-----------|----------------|
| Customer accessing `/api/v1/audit` | 403 |
| Customer accessing `/api/v1/users` (GET all) | 403 |
| Customer accessing `POST /api/v1/accounts` | 403 |
| Customer accessing `POST /api/v1/transactions/deposit` | 403 |
| Employee accessing `POST /api/v1/transactions/{id}/reverse` | 403 |
| Employee accessing `PATCH /api/v1/accounts/{id}/freeze` | 403 |
| Accessing any endpoint without token | 401 |
| Accessing with blacklisted token | 401 |

---

## 12. Security Testing

### 12.1 Role-Based Access Control Matrix

| Endpoint | ADMIN | EMPLOYEE | TELLER | CUSTOMER | Anonymous |
|----------|-------|----------|--------|----------|-----------|
| `POST /auth/register` | Y | Y | Y | Y | Y |
| `POST /auth/login` | Y | Y | Y | Y | Y |
| `GET /users/profile` | Y | Y | Y | Y | N |
| `GET /users` | Y | N | N | N | N |
| `PATCH /users/{id}/lock` | Y | N | N | N | N |
| `POST /accounts` | Y | Y | N | N | N |
| `GET /accounts/my-accounts` | Y | Y | Y | Y | N |
| `PATCH /accounts/{id}/freeze` | Y | N | N | N | N |
| `POST /transactions/deposit` | Y | N | Y | N | N |
| `POST /transactions/withdraw` | Y | N | Y | N | N |
| `POST /transactions/transfer` | Y | N | Y | Y | N |
| `POST /transactions/{id}/reverse` | Y | N | N | N | N |
| `GET /transactions` | Y | N | Y | N | N |
| `GET /transactions/my-transactions` | N | N | N | Y | N |
| `GET /notifications` | Y | Y | Y | Y | N |
| `GET /audit` | Y | N | N | N | N |

### 12.2 Privilege Escalation Tests

| Test | Method |
|------|--------|
| CUSTOMER tries to create account | Send `POST /api/v1/accounts` with customer token → expect 403 |
| CUSTOMER tries to deposit | Send `POST /api/v1/transactions/deposit` with customer token → expect 403 |
| EMPLOYEE tries to freeze account | Send `PATCH /api/v1/accounts/{id}/freeze` with employee token → expect 403 |
| EMPLOYEE tries to reverse transaction | Send `POST /api/v1/transactions/{id}/reverse` with employee token → expect 403 |
| EMPLOYEE tries to view audit logs | Send `GET /api/v1/audit` with employee token → expect 403 |

### 12.3 Rate Limiting Tests

Send more than the configured limit to trigger 429:

| Endpoint | Limit | How to Test |
|----------|-------|-------------|
| `POST /auth/login` | 5/60s | Send 6 login requests within 60s |
| `POST /auth/register` | 10/60s | Send 11 registration requests within 60s |
| `POST /auth/forgot-password` | 3/300s | Send 4 requests within 5 min |
| `POST /transactions/deposit` | 20/60s | Send 21 deposit requests |

**Response (429):**
```json
{
    "success": false,
    "message": "Rate limit exceeded. Please try again later.",
    "timestamp": "2026-07-25T10:00:00"
}
```

**Headers:**
```
X-Rate-Limit-Remaining: 0
X-Rate-Limit-Retry-After-Seconds: 45
```

---

## 13. Database Verification

### After Registration

```sql
SELECT * FROM users WHERE email = 'john.doe@example.com';
-- enabled=0, account_non_locked=1, failed_attempts=0

SELECT * FROM verification_tokens WHERE user_id = (SELECT id FROM users WHERE email='...');
-- token, expiry_date (+24h), verified=0

SELECT * FROM otp_codes WHERE user_id = (SELECT id FROM users WHERE email='...');
-- code (6 digits), purpose='EMAIL_VERIFICATION', verified=0

SELECT * FROM user_roles WHERE user_id = (SELECT id FROM users WHERE email='...');
-- role_id = ROLE_CUSTOMER ID
```

### After Email Verification

```sql
SELECT enabled FROM users WHERE email = '...';  -- 1
SELECT verified FROM verification_tokens WHERE ...;  -- 1
```

### After Login

```sql
SELECT failed_attempts, last_login FROM users WHERE email = '...';  -- 0, updated
SELECT * FROM refresh_tokens WHERE user_id = ...;  -- new row
```

### After Deposit

```sql
SELECT balance FROM accounts WHERE account_number = '...';  -- increased
SELECT * FROM transactions WHERE account_id = ... ORDER BY created_at DESC LIMIT 1;
-- type=DEPOSIT, status=SUCCESS, reference_number like 'TXN...'
```

### After Transfer

```sql
SELECT balance FROM accounts WHERE account_number = 'sender';  -- decreased
SELECT balance FROM accounts WHERE account_number = 'receiver';  -- increased
SELECT daily_transferred_amount FROM accounts WHERE account_number = 'sender';  -- increased
SELECT * FROM transactions WHERE reference_number = '...';
-- type=TRANSFER, status=SUCCESS
```

### After Account Freeze

```sql
SELECT status FROM accounts WHERE id = '...';  -- FROZEN
```

---

## 14. Redis Verification

### Redis Keys

| Key Pattern | TTL | Purpose |
|-------------|-----|---------|
| `jwt:blacklist:<token>` | Token expiry (15 min) | Blacklisted JWT tokens |
| `login:attempt:<email>` | 15 minutes | Failed login attempt count |

### Check in Redis CLI

```bash
# Check blacklisted tokens
redis-cli KEYS "jwt:blacklist:*"

# Check login attempts
redis-cli KEYS "login:attempt:*"
redis-cli GET "login:attempt:john.doe@example.com"
```

### Cache Entries (In-Memory Mode)

| Cache Name | TTL | Keys |
|------------|-----|------|
| `users` | 3600s | Email, `id::<uuid>` |
| `accounts` | 1800s | Account UUID |
| `notificationCount` | 300s | User UUID |

---

## 15. Email Verification

### In Development

Use **MailHog** or **Mailtrap** as SMTP server. Configure in `application.properties`:

```properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
```

### Verify Email Was Sent

1. **MailHog UI:** `http://localhost:8025` — check inbox for verification email
2. **SMTP logs:** Check application logs for email sending attempts
3. **Database:** Check `notifications` table for `type=EMAIL` and `status=SENT`

### Verify OTP Was Sent

Check `otp_codes` table:
```sql
SELECT code, purpose, expiry_time, verified FROM otp_codes
WHERE user_id = (SELECT id FROM users WHERE email = '...')
ORDER BY created_at DESC LIMIT 1;
```

---

## 16. Postman Automation

### 16.1 Pre-request Scripts

**For all authenticated requests — Pre-request Script:**

```javascript
// Ensure token exists
if (!pm.environment.get("accessToken")) {
    throw new Error("No access token available. Please login first.");
}
```

### 16.2 Collection-Level Tests

**Collection Test — Validate Common Response Structure:**

```javascript
pm.test("Response has success field", function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property("success");
});

pm.test("Response has timestamp", function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property("timestamp");
});
```

### 16.3 Complete Test Scripts Per Request

**Register — Tests:**

```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response indicates success", function () {
    const json = pm.response.json();
    pm.expect(json.success).to.be.true;
});

pm.test("Verification token is returned", function () {
    const json = pm.response.json();
    pm.expect(json.data).to.have.property("token");
    pm.environment.set("verificationToken", json.data.token);
});
```

**Login — Tests:**

```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Tokens are returned", function () {
    const json = pm.response.json();
    pm.expect(json.data).to.have.property("accessToken");
    pm.expect(json.data).to.have.property("refreshToken");
    pm.environment.set("accessToken", json.data.accessToken);
    pm.environment.set("refreshToken", json.data.refreshToken);
    pm.environment.set("userId", json.data.user.id);
});

pm.test("Token type is Bearer", function () {
    const json = pm.response.json();
    pm.expect(json.data.tokenType).to.equal("Bearer");
});

pm.test("Expires in is 900 seconds", function () {
    const json = pm.response.json();
    pm.expect(json.data.expiresIn).to.equal(900);
});
```

**Create Account — Tests:**

```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Account data returned", function () {
    const json = pm.response.json();
    pm.expect(json.data).to.have.property("id");
    pm.expect(json.data).to.have.property("accountNumber");
    pm.expect(json.data.accountNumber).to.have.lengthOf(10);
    pm.environment.set("accountId", json.data.id);
    pm.environment.set("accountNumber", json.data.accountNumber);
});
```

**Deposit — Tests:**

```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Transaction recorded", function () {
    const json = pm.response.json();
    pm.expect(json.data.transactionType).to.equal("DEPOSIT");
    pm.expect(json.data.status).to.equal("SUCCESS");
    pm.expect(json.data.referenceNumber).to.match(/^TXN/);
    pm.environment.set("transactionId", json.data.id);
    pm.environment.set("referenceNumber", json.data.referenceNumber);
});
```

**Transfer — Tests:**

```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Transfer recorded correctly", function () {
    const json = pm.response.json();
    pm.expect(json.data.transactionType).to.equal("TRANSFER");
    pm.expect(json.data.status).to.equal("SUCCESS");
    pm.expect(json.data.senderAccountNumber).to.equal(pm.environment.get("accountNumber"));
    pm.environment.set("transactionId", json.data.id);
    pm.environment.set("referenceNumber", json.data.referenceNumber);
});
```

**Notifications — Tests:**

```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Paginated response", function () {
    const json = pm.response.json();
    pm.expect(json.data).to.have.property("content");
    pm.expect(json.data).to.have.property("unreadCount");
    if (json.data.content.length > 0) {
        pm.environment.set("notificationId", json.data.content[0].id);
    }
});
```

**Refresh Token — Tests:**

```javascript
pm.test("New tokens received", function () {
    const json = pm.response.json();
    pm.expect(json.data).to.have.property("accessToken");
    pm.expect(json.data).to.have.property("refreshToken");
    pm.environment.set("accessToken", json.data.accessToken);
    pm.environment.set("refreshToken", json.data.refreshToken);
});
```

**Logout — Tests:**

```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Logout successful", function () {
    const json = pm.response.json();
    pm.expect(json.data.message).to.include("Logged out");
});
```

---

## 17. Collection Runner

### Setup

1. Create a Postman Collection called **"Nexus Bank API"**
2. Add requests in the order specified in Section 3
3. Create an Environment called **"Nexus Bank"** with the variables from Section 2
4. Set the collection's base URL variable

### Runner Execution

1. Open Postman → Runner
2. Select **"Nexus Bank API"** collection
3. Select **"Nexus Bank"** environment
4. Set iterations to **1** (or more for stress testing)
5. Run

### Dependency Chain

The collection must follow this exact dependency chain:

```
Register (admin) → Verify Email → Login (admin)
    ↓
Register (customer) → Verify Email → Login (customer)
    ↓
Create Account (admin, for customer) → Deposit → Withdraw
    ↓
Create 2nd Account → Deposit → Transfer → Verify Balances
    ↓
Get Notifications → Mark Read → Delete
    ↓
Get Profile → Update Profile → Change Password
    ↓
Get All Users (admin) → Lock/Unlock User (admin)
    ↓
Get Audit Logs (admin)
    ↓
Refresh Token → Logout → Verify Blacklisted Token
    ↓
Forgot Password → Verify OTP → Reset Password → Login with New Password
    ↓
Freeze Account → Verify Frozen (deposit should fail) → Close Account
```

### Parallel Execution Warning

**DO NOT** run requests in parallel — they share state via environment variables. Each request must complete before the next.

---

## 18. Final Acceptance Checklist

### Authentication (8/8 endpoints)

- [ ] Register — success
- [ ] Register — duplicate email (409)
- [ ] Register — weak password (400)
- [ ] Register — passwords mismatch (400)
- [ ] Verify Email — success
- [ ] Verify Email — expired token (400)
- [ ] Verify Email — used token (400)
- [ ] Login — success
- [ ] Login — wrong password (400)
- [ ] Login — unverified account (400)
- [ ] Login — locked account (400)
- [ ] Login — account locks after 5 failures
- [ ] Refresh Token — success (rotation)
- [ ] Refresh Token — revoked token (400)
- [ ] Refresh Token — expired token (400)
- [ ] Logout — success (blacklist + revoke)
- [ ] Logout — blacklisted token rejected
- [ ] Forgot Password — returns success regardless
- [ ] Verify OTP — success
- [ ] Verify OTP — wrong code (400)
- [ ] Verify OTP — expired (400)
- [ ] Reset Password — success
- [ ] Reset Password — all refresh tokens revoked
- [ ] Change Password — success
- [ ] Change Password — wrong current password (400)

### Users (10/10 endpoints)

- [ ] Get Profile — success
- [ ] Update Profile — success
- [ ] Update Profile — duplicate phone (409)
- [ ] Get User by ID — ADMIN success
- [ ] Get User by ID — CUSTOMER forbidden (403)
- [ ] Get All Users — ADMIN success with pagination
- [ ] Delete User — ADMIN success
- [ ] Lock User — success + event fired
- [ ] Unlock User — success
- [ ] Enable User — success
- [ ] Disable User — success

### Accounts (7/7 endpoints)

- [ ] Create Account — ADMIN success
- [ ] Create Account — EMPLOYEE success
- [ ] Create Account — duplicate IBAN (409)
- [ ] Get Account by ID — success
- [ ] Get Account by Number — success
- [ ] Get Accounts by User ID — ADMIN success
- [ ] Get Accounts by User ID — owner success
- [ ] Get Accounts by User ID — non-owner forbidden (403)
- [ ] Get My Accounts — success
- [ ] Freeze Account — ADMIN success + notification
- [ ] Close Account — ADMIN success + notification

### Transactions (8/8 endpoints)

- [ ] Deposit — success + balance increased
- [ ] Deposit — inactive account (400)
- [ ] Withdraw — success + balance decreased
- [ ] Withdraw — insufficient balance (400)
- [ ] Withdraw — inactive account (400)
- [ ] Transfer — success + both balances correct
- [ ] Transfer — same account (400)
- [ ] Transfer — insufficient balance (400)
- [ ] Transfer — daily limit exceeded (400)
- [ ] Reverse Deposit — balance decreased
- [ ] Reverse Withdraw — balance increased
- [ ] Reverse Transfer — both balances restored
- [ ] Reverse already reversed (400)
- [ ] Reverse non-SUCCESS (400)
- [ ] Get Transaction by ID — success
- [ ] Get Transaction by Reference — success
- [ ] Transaction History — ADMIN with filters
- [ ] My Transactions — CUSTOMER success

### Notifications (6/6 endpoints)

- [ ] Get Notifications — success with unreadCount
- [ ] Get Notification by ID — success
- [ ] Get Notification by ID — wrong user (403)
- [ ] Get Unread Notifications — success
- [ ] Mark As Read — success + readAt set
- [ ] Mark All As Read — success
- [ ] Delete Notification — success

### Audit (3/3 endpoints)

- [ ] Get All Audit Logs — ADMIN success with pagination
- [ ] Get Audit Logs by User — success
- [ ] Get Audit Logs by Event Type — success
- [ ] Audit logs exist for registration
- [ ] Audit logs exist for login
- [ ] Audit logs exist for transactions

### Security

- [ ] 401 for unauthenticated requests
- [ ] 403 for insufficient role
- [ ] 429 for rate limit exceeded
- [ ] JWT blacklist working after logout
- [ ] Refresh token rotation working
- [ ] Account locking after 5 failed logins

### Rate Limiting

- [ ] Login: 5 requests per 60s
- [ ] Register: 10 requests per 60s
- [ ] Forgot Password: 3 requests per 300s
- [ ] Reset Password: 3 requests per 300s
- [ ] Deposit: 20 requests per 60s
- [ ] Withdraw: 10 requests per 60s
- [ ] Transfer: 20 requests per 60s
- [ ] Account Create: 5 requests per 300s

### Events & Notifications

- [ ] UserRegistered → Welcome notification + OTP email
- [ ] EmailVerified → Email Verified notification
- [ ] UserLoggedIn → Login notification
- [ ] MoneyDeposited → Deposit notification
- [ ] MoneyWithdrawn → Withdrawal notification
- [ ] MoneyTransferred → Send + Receive notifications
- [ ] AccountFrozen → Frozen email notification
- [ ] AccountClosed → Closed email notification
- [ ] PasswordChanged → Password Changed notification
- [ ] PasswordReset → Password Reset notification

### Database Integrity

- [ ] All UUIDs are valid BINARY(16)
- [ ] All timestamps use DATETIME(6)
- [ ] Account balance never goes negative (CHECK constraint)
- [ ] Transaction amount always > 0 (CHECK constraint)
- [ ] All foreign keys properly cascade
- [ ] Optimistic locking version column functional

### Schedulers

- [ ] `ExpiredOtpCleanupJob` — runs at 3 AM
- [ ] `ExpiredVerificationTokenCleanupJob` — runs at 3 AM
- [ ] `ExpiredRefreshTokenCleanupJob` — runs at 2 AM
- [ ] `OldNotificationCleanupJob` — runs at 4 AM (90-day retention)
- [ ] `OldAuditCleanupJob` — runs at 4 AM (365-day retention)
- [ ] `DailyTransferLimitResetJob` — runs at midnight
- [ ] `CacheCleanupJob` — runs at 1 AM

### Swagger

- [ ] Swagger UI loads at `/swagger-ui.html`
- [ ] All 41 endpoints documented
- [ ] Bearer token authentication works from Swagger
- [ ] Request/response models visible

### API Response Consistency

- [ ] All success responses use `ApiResponse<T>` wrapper with `success=true`
- [ ] All error responses use `ApiError` wrapper with `success=false`
- [ ] All responses include `timestamp`
- [ ] All error responses include `path`
- [ ] Proper HTTP status codes used (200, 201, 400, 401, 403, 404, 409, 429, 500)

---

**End of Guide.** Total endpoints covered: **41**. Total negative test cases: **50+**. Total checklist items: **100+**.
