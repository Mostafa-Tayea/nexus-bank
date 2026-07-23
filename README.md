<div align="center">

# 🏦 Nexus Bank

### Enterprise Banking System — Production-Grade Backend API

A full-featured, event-driven banking platform built with **Java 21** and **Spring Boot 4.1.0**.
Implements secure authentication, role-based authorization, multi-account management,
real-time transaction processing, and comprehensive audit logging.

<br/>

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-7.1.0-6CAB3B?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)](#)
[![Tests](https://img.shields.io/badge/Tests-43%20Passing-brightgreen?style=for-the-badge)](#testing)

<br/>

</div>

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Technology Stack](#-technology-stack)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Authentication Flow](#-authentication-flow)
- [Business Modules](#-business-modules)
- [Database](#-database)
- [Security](#-security)
- [Event-Driven Design](#-event-driven-design)
- [Caching](#-caching)
- [Scheduler](#-scheduler)
- [Error Handling](#-error-handling)
- [API Examples](#-api-examples)
- [Testing](#-testing)
- [Performance](#-performance)
- [Future Improvements](#-future-improvements)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🔍 Overview

**Nexus Bank** is a backend API for a modern banking system. It provides a secure, scalable foundation for user management, multi-currency accounts, fund transfers, and financial transaction processing.

The system follows a **modular monolith** architecture with **package-by-feature** organization, **event-driven** side-effect processing, and **layered service design** — ready to be decomposed into microservices when scaling demands.

### Why This Project?

- Production-oriented patterns: optimistic locking, daily transfer limits, account freezing, OTP verification
- Enterprise security: JWT with refresh tokens, Redis-based blacklist, BCrypt, rate limiting, role-based access
- Comprehensive audit trail for every financial event
- Clean, testable codebase with 43 passing unit tests

---

## ✨ Features

<table>
<tr><td>

**Authentication & Security**
- 🔐 JWT Access & Refresh Tokens
- 📧 Email Verification
- 🔢 OTP Codes (SecureRandom)
- 🔑 Password Reset Flow
- 👥 Role-Based Authorization (ADMIN, EMPLOYEE, CUSTOMER)
- 🚦 Rate Limiting (Bucket4j)
- 🛡️ Redis Token Blacklist
- 🔒 BCrypt Password Encryption (strength 12)
- 🧱 Method-Level Security (`@PreAuthorize`)

</td><td>

**Core Banking**
- 💳 Multi-Account Management (SAVINGS, CURRENT)
- 💰 Deposit, Withdraw & Transfer Operations
- 🔄 Transaction Reversals
- 📊 Daily Transfer Limits
- 🧊 Account Freeze & Close
- 🔐 Optimistic Locking (`@Version`)
- 🔒 Pessimistic Locking for Balance Operations
- 📄 Pagination, Filtering & Search

</td></tr>
<tr><td>

**Infrastructure**
- 🗄️ MySQL with Flyway Migrations (11 migrations)
- ⚡ Redis Caching (optional, with in-memory fallback)
- 📬 Email Notifications (Spring Mail)
- 📋 Audit Logging
- ⏰ Scheduled Cleanup Jobs (7 jobs)
- 📖 Swagger / OpenAPI 3.0

</td><td>

**Developer Experience**
- 🧪 JUnit 5 + Mockito Tests
- 📐 MapStruct DTO Mapping
- 🧬 Lombok Code Generation
- 🌐 CORS Configuration
- 📝 Global Exception Handling
- 🔧 Environment-Variable Configuration

</td></tr>
</table>

---

## 🏗️ Architecture

### Design Principles

| Principle | Implementation |
|-----------|---------------|
| **Package-by-Feature** | Each domain (auth, account, transaction, etc.) is a self-contained package |
| **Layered Architecture** | Controller → Service → Repository with clear separation of concerns |
| **Event-Driven Design** | 15 domain events decouple side-effects (notifications, audits) from business logic |
| **SOLID Principles** | Interface-based services, single-responsibility listeners, open/closed exception hierarchy |
| **Domain Events** | Java records as immutable event carriers via `ApplicationEventPublisher` |

### High-Level Architecture

```mermaid
graph TB
    Client[Client / Frontend] -->|HTTP/REST| Gateway[Security Filter Chain]

    Gateway --> RL[RateLimitFilter<br/>Bucket4j]
    RL --> JWT[JwtAuthenticationFilter]
    JWT --> UPF[UsernamePassword<br/>AuthenticationFilter]

    UPF --> AuthMod[Auth Module]
    UPF --> UserMod[User Module]
    UPF --> AccMod[Account Module]
    UPF --> TxMod[Transaction Module]
    UPF --> NotifMod[Notification Module]
    UPF --> AuditMod[Audit Module]

    AuthMod --> DB[(MySQL)]
    UserMod --> DB
    AccMod --> DB
    TxMod --> DB
    NotifMod --> DB
    AuditMod --> DB

    AuthMod --> Cache[(Redis / Cache)]
    TxMod --> Cache

    TxMod -->|publishEvent| EB[Application Event Bus]
    AuthMod -->|publishEvent| EB
    AccMod -->|publishEvent| EB

    EB --> NL[NotificationListener<br/>@Async]
    EB --> AL[AuditListener<br/>@Async]

    NL --> NotifMod
    AL --> AuditMod
```

### Request Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant RF as RateLimitFilter
    participant JF as JwtFilter
    participant SC as SecurityContext
    participant Ctrl as Controller
    participant Svc as Service
    participant Repo as Repository
    participant DB as MySQL
    participant EB as Event Bus

    C->>RF: HTTP Request
    RF->>RF: Check Rate Limit
    alt Rate Limit Exceeded
        RF-->>C: 429 Too Many Requests
    else Allowed
        RF->>JF: Pass
        JF->>JF: Extract & Validate JWT
        alt Valid Token
            JF->>SC: Set Authentication
        end
        JF->>Ctrl: Forward Request
        Ctrl->>Svc: Business Logic
        Svc->>Repo: Data Access
        Repo->>DB: SQL Query
        DB-->>Repo: Result
        Repo-->>Svc: Entity
        Svc->>Svc: Process Business Rules
        Svc->>EB: Publish Domain Event
        Svc-->>Ctrl: Response DTO
        Ctrl-->>C: JSON Response
    end
```

---

## 📂 Project Structure

```
src/main/java/com/mostafa/nexus_bank/
│
├── NexusBankApplication.java                 # Entry point (@EnableScheduling, @EnableAsync)
│
├── account/                                  # 💳 Account Management
│   ├── controller/AccountController.java
│   ├── dto/request/  CreateAccountRequest
│   ├── dto/response/ AccountResponse
│   ├── entity/Account.java
│   ├── repository/AccountRepository
│   └── service/AccountService.java
│
├── audit/                                    # 📋 Audit Logging
│   ├── controller/AuditController.java
│   ├── dto/response/ AuditResponse, AuditPageResponse
│   ├── entity/Audit.java
│   ├── listener/AuditListener.java
│   ├── mapper/AuditMapper
│   ├── repository/AuditRepository
│   └── service/ AuditService, AuditServiceImpl
│
├── auth/                                     # 🔐 Authentication
│   ├── controller/AuthController.java
│   ├── dto/request/  LoginRequest, RegisterRequest, VerifyEmailRequest,
│   │                  VerifyOtpRequest, ForgotPasswordRequest, ResetPasswordRequest,
│   │                  RefreshTokenRequest, LogoutRequest
│   ├── dto/response/ AuthenticationResponse, MessageResponse, RefreshTokenResponse
│   ├── entity/ OtpCode, RefreshToken, VerificationToken
│   ├── repository/ OtpCodeRepository, RefreshTokenRepository, VerificationTokenRepository
│   └── service/ AuthService, AuthServiceImpl
│
├── cache/                                    # ⚡ Caching
│   ├── config/ CacheNames, CacheTtlProperties, RedisConfig
│   └── service/ JwtBlacklistService, LoginAttemptService
│
├── common/                                   # 📦 Shared
│   ├── entity/BaseEntity                     # UUID id, createdAt, updatedAt, version
│   ├── enums/ AccountStatus, AccountType, NotificationStatus,
│   │          NotificationType, OtpPurpose, RoleType, TransactionStatus, TransactionType
│   ├── event/ 15 event records               # Domain events
│   ├── exception/ GlobalExceptionHandler
│   └── response/ ApiError, ApiResponse, ErrorResponse
│
├── config/                                   # 🔧 Configuration
│   └── OpenApiConfig                         # Swagger / OpenAPI
│
├── exception/                                # ⚠️ Business Exceptions (14 types)
│   ├── BusinessException                     # Base exception
│   ├── AccountLockedException
│   ├── DuplicateResourceException
│   ├── EmailSendingException
│   ├── EntityNotFoundException
│   ├── ForbiddenException
│   ├── InsufficientBalanceException
│   ├── InvalidOtpException
│   ├── InvalidTokenException
│   ├── NotificationException
│   ├── ResourceConflictException
│   ├── TransferLimitExceededException
│   ├── UnauthorizedException
│   └── ValidationException
│
├── notification/                             # 📬 Notifications
│   ├── controller/NotificationController.java
│   ├── dto/response/ NotificationResponse, NotificationPageResponse
│   ├── entity/Notification.java
│   ├── listener/NotificationListener.java
│   ├── mapper/NotificationMapper
│   ├── repository/NotificationRepository
│   └── service/ NotificationService, NotificationServiceImpl
│
├── role/                                     # 👥 Roles
│   ├── dto/response/RoleResponse
│   ├── entity/Role.java
│   ├── mapper/RoleMapper
│   └── repository/RoleRepository
│
├── scheduler/                                # ⏰ Scheduled Jobs
│   ├── config/SchedulerProperties
│   └── job/
│       ├── CacheCleanupJob
│       ├── DailyTransferLimitResetJob
│       ├── ExpiredOtpCleanupJob
│       ├── ExpiredRefreshTokenCleanupJob
│       ├── ExpiredVerificationTokenCleanupJob
│       ├── OldAuditCleanupJob
│       └── OldNotificationCleanupJob
│
├── security/                                 # 🛡️ Security
│   ├── config/ AuthProperties, CorsConfig, PasswordConfig, SecurityConfig
│   ├── filter/ RateLimitFilter, RateLimitProperties
│   ├── jwt/ JwtAccessDeniedHandler, JwtAuthenticationEntryPoint,
│   │        JwtAuthenticationFilter, JwtProperties, JwtService
│   ├── service/ CustomUserDetails, CustomUserDetailsService, RefreshTokenService
│   └── util/ TokenProvider
│
├── transaction/                              # 💸 Transactions
│   ├── controller/TransactionController.java
│   ├── dto/request/  DepositRequest, TransferRequest, WithdrawRequest
│   ├── dto/response/ TransactionResponse, TransactionPageResponse
│   ├── entity/Transaction.java
│   ├── mapper/TransactionMapper
│   ├── repository/TransactionRepository
│   ├── specification/TransactionSpecification
│   └── service/ TransactionService, TransactionServiceImpl
│
├── user/                                     # 👤 User Management
│   ├── controller/UserController.java
│   ├── dto/request/  CreateUserRequest, UpdateUserRequest, ChangePasswordRequest
│   ├── dto/response/ UserResponse, UserProfileResponse, UserPageResponse
│   ├── entity/User.java
│   ├── mapper/UserMapper
│   ├── repository/UserRepository
│   ├── specification/UserSpecification
│   └── service/ UserService, UserServiceImpl
│
└── validation/                               # ✅ Custom Validators
    ├── IBANValidator, ValidIBAN
    ├── NationalIdValidator, ValidNationalId
    ├── PasswordValidator, ValidPassword
    └── PhoneNumberValidator, ValidPhoneNumber
```

---

## 🛠️ Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 4.1.0 |
| **Security** | Spring Security | 7.1.0 |
| **ORM** | Spring Data JPA (Hibernate) | — |
| **Database** | MySQL | 8.x |
| **Migrations** | Flyway | — |
| **Cache** | Redis (optional) | 7.x |
| **JWT** | JJWT | 0.12.6 |
| **Rate Limiting** | Bucket4j | 8.19.0 |
| **API Docs** | SpringDoc OpenAPI | 3.0.3 |
| **Mapping** | MapStruct | 1.6.3 |
| **Boilerplate** | Lombok | — |
| **Email** | Spring Boot Starter Mail | — |
| **Build** | Maven | Wrapper |
| **Testing** | JUnit 5 + Mockito | — |

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version | Purpose |
|------------|---------|---------|
| Java | 21+ | Runtime |
| MySQL | 8.x | Primary database |
| Redis | 7.x (optional) | Caching & token blacklist |
| Maven | Wrapper included | Build tool |

### 1. Clone the Repository

```bash
git clone https://github.com/mostafa/nexus-bank.git
cd nexus-bank
```

### 2. Database Setup

```sql
CREATE DATABASE nexus_bank;
CREATE USER 'nexus_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON nexus_bank.* TO 'nexus_user'@'localhost';
FLUSH PRIVILEGES;
```

> **Note:** Flyway will automatically create all tables and seed data on first run.

### 3. Configure Environment Variables

```bash
# Database
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=nexus_bank
export DB_USERNAME=nexus_user
export DB_PASSWORD=your_password

# JWT Secret (use a strong random string in production)
export JWT_SECRET=your-256-bit-secret-key-here-change-in-production
```

### 4. Run the Application

```bash
# Using Maven Wrapper (Windows)
.\mvnw.cmd spring-boot:run

# Using Maven Wrapper (Linux/Mac)
./mvnw spring-boot:run

# Or build and run the JAR
.\mvnw.cmd clean package -DskipTests
java -jar target/nexus-bank-0.0.1-SNAPSHOT.jar
```

The application starts on **`http://localhost:8080`**.

### 5. Verify

| Endpoint | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/v3/api-docs |
| Health Check | http://localhost:8080/actuator/health |

---

## ⚙️ Configuration

All configuration is in `src/main/resources/application.properties`. Secrets are externalized via environment variables.

<details>
<summary><b>🗄️ Database</b></summary>

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:test}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```
</details>

<details>
<summary><b>🔐 JWT</b></summary>

```properties
jwt.secret=${JWT_SECRET:default-dev-secret-change-in-production}
jwt.access-token.expiration=900000          # 15 minutes
jwt.refresh-token.expiration=604800000      # 7 days
```
</details>

<details>
<summary><b>⚡ Cache</b></summary>

```properties
spring.cache.type=simple                    # Switch to 'redis' when Redis is available

cache.ttl.user=3600
cache.ttl.account=1800
cache.ttl.role=7200
cache.ttl.otp=900
cache.ttl.jwt-blacklist=3600
```
</details>

<details>
<summary><b>🚦 Rate Limiting</b></summary>

```properties
rate-limit.endpoints.auth.register.capacity=10
rate-limit.endpoints.auth.register.refill-tokens=10
rate-limit.endpoints.auth.register.refill-duration-seconds=60

rate-limit.endpoints.auth.login.capacity=5
rate-limit.endpoints.auth.login.refill-tokens=5
rate-limit.endpoints.auth.login.refill-duration-seconds=60

rate-limit.endpoints.transaction.deposit.capacity=20
rate-limit.endpoints.transaction.withdraw.capacity=10
rate-limit.endpoints.transaction.transfer.capacity=20
```
</details>

<details>
<summary><b>⏰ Scheduler</b></summary>

```properties
scheduler.otp-cleanup.cron=0 0 3 * * ?              # 3:00 AM daily
scheduler.verification-token-cleanup.cron=0 0 3 * * ?  # 3:00 AM daily
scheduler.refresh-token-cleanup.cron=0 0 2 * * ?     # 2:00 AM daily
scheduler.notification-cleanup.cron=0 0 4 * * ?      # 4:00 AM daily
scheduler.audit-cleanup.cron=0 0 4 * * ?             # 4:00 AM daily
scheduler.transfer-limit-reset.cron=0 0 0 * * ?      # Midnight daily
scheduler.cache-cleanup.cron=0 0 1 * * ?             # 1:00 AM daily
```
</details>

---

## 📖 API Documentation

Swagger UI is available at **`http://localhost:8080/swagger-ui.html`** after starting the application.

### Authentication

Most endpoints require a valid JWT access token. Include it in the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Endpoints Summary

| Module | Base Path | Methods | Auth |
|--------|-----------|---------|------|
| **Auth** | `/api/v1/auth` | Register, Login, Logout, Refresh, Verify, Reset | Public / Authenticated |
| **Users** | `/api/v1/users` | CRUD, Profile, Lock/Unlock | ADMIN / Self |
| **Accounts** | `/api/v1/accounts` | Create, Read, Freeze, Close | ADMIN, EMPLOYEE |
| **Transactions** | `/api/v1/transactions` | Deposit, Withdraw, Transfer, Reverse | ADMIN, EMPLOYEE |
| **Notifications** | `/api/v1/notifications` | List, Read, Delete | Authenticated |
| **Audit** | `/api/v1/audit` | List, Filter by User/Event | ADMIN only |

---

## 🔑 Authentication Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant A as Auth Service
    participant D as Database
    participant M as Mail Service
    participant E as Event Bus

    Note over C,E: 📝 Registration
    C->>A: POST /auth/register
    A->>D: Save User
    A->>E: Publish UserRegisteredEvent
    A->>E: Publish OtpGeneratedEvent
    A-->>C: 201 "Registration successful"

    Note over C,E: 📧 Email Verification
    C->>A: POST /auth/verify-email
    A->>D: Find VerificationToken
    A->>D: Enable User
    A->>E: Publish EmailVerifiedEvent
    A-->>C: 200 "Email verified"

    Note over C,E: 🔐 Login
    C->>A: POST /auth/login
    A->>D: Find User, Validate Password
    A->>D: Save RefreshToken
    A->>E: Publish UserLoggedInEvent
    A-->>C: 200 {accessToken, refreshToken}

    Note over C,E: 🔄 Refresh Token
    C->>A: POST /auth/refresh-token
    A->>D: Validate RefreshToken
    A->>D: Save New RefreshToken
    A-->>C: 200 {newAccessToken, newRefreshToken}

    Note over C,E: 🚪 Logout
    C->>A: POST /auth/logout
    A->>D: Revoke RefreshToken
    A->>Cache: Blacklist AccessToken
    A-->>C: 200 "Logged out"

    Note over C,E: 🔑 Password Reset
    C->>A: POST /auth/forgot-password
    A->>E: Publish OtpGeneratedEvent
    A-->>C: 200 "OTP sent"

    C->>A: POST /auth/reset-password
    A->>D: Validate OTP, Update Password
    A->>E: Publish PasswordResetEvent
    A-->>C: 200 "Password reset"
```

---

## 📦 Business Modules

### 👤 User Module
- Registration with validation (email, phone, national ID)
- Profile management (update personal info)
- Password change / reset with OTP
- Admin controls: lock, unlock, enable, disable
- Paginated user listing with search

### 💳 Account Module
- Create accounts (SAVINGS / CURRENT) with unique account numbers and IBAN
- Account status lifecycle: ACTIVE → FROZEN / CLOSED / SUSPENDED
- Per-account daily transfer limits
- Balance validation (CHECK constraint: `balance >= 0`)

### 💸 Transaction Module
- **Deposit** — Add funds to an account
- **Withdraw** — Remove funds with balance validation
- **Transfer** — Move funds between accounts with daily limit enforcement
- **Reverse** — Reverse completed transactions (admin only)
- Pessimistic locking for concurrent balance operations
- Optimistic locking (`@Version`) for entity-level conflict detection
- Specification-based filtering, pagination, and search
- Reference number generation for each transaction

### 📬 Notification Module
- Multi-type notifications: EMAIL, SMS, PUSH, IN_APP
- Status tracking: PENDING → SENT / FAILED → READ / UNREAD
- Automatic creation via event listeners
- Read / mark-all-read / delete operations
- Unread count endpoint

### 📋 Audit Module
- Automatic logging of 13 event types via `AuditListener`
- Records: event type, action, username, user ID, IP, device, HTTP method, endpoint, result
- Paginated audit log querying
- Filter by user or event type
- Configurable retention (default: 365 days)

---

## 🗄️ Database

### Entity Relationship Diagram

```mermaid
erDiagram
    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : assigned_to
    USER ||--o{ ACCOUNT : owns
    USER ||--o{ REFRESH_TOKEN : has
    USER ||--o{ VERIFICATION_TOKEN : has
    USER ||--o{ OTP_CODE : has
    USER ||--o{ NOTIFICATION : receives
    ACCOUNT ||--o{ TRANSACTION : sender
    ACCOUNT ||--o{ TRANSACTION : receiver

    USER {
        uuid id PK
        string firstName
        string lastName
        string email UK
        string phone UK
        string nationalId UK
        string password
        boolean enabled
        boolean accountNonLocked
        int failedAttempts
        datetime lastLogin
        datetime createdAt
        datetime updatedAt
        int version
    }

    ROLE {
        uuid id PK
        enum name UK
        string description
    }

    ACCOUNT {
        uuid id PK
        string accountNumber UK
        string iban UK
        decimal balance
        string currency
        enum type
        enum status
        decimal dailyTransferLimit
        decimal dailyTransferredAmount
        uuid userId FK
    }

    TRANSACTION {
        uuid id PK
        string referenceNumber UK
        decimal amount
        enum transactionType
        enum status
        string description
        uuid senderAccountId FK
        uuid receiverAccountId FK
    }

    REFRESH_TOKEN {
        uuid id PK
        string token UK
        datetime expiryDate
        boolean revoked
        uuid userId FK
    }

    VERIFICATION_TOKEN {
        uuid id PK
        string token UK
        datetime expiryDate
        boolean verified
        datetime verifiedAt
        uuid userId FK
    }

    OTP_CODE {
        uuid id PK
        string code
        enum purpose
        datetime expiryTime
        boolean verified
        uuid userId FK
    }

    NOTIFICATION {
        uuid id PK
        string title
        string message
        enum type
        enum status
        datetime sentAt
        datetime readAt
        int retryCount
        uuid userId FK
    }

    AUDIT {
        uuid id PK
        string eventType
        string action
        string username
        uuid userId
        string ipAddress
        string device
        string httpMethod
        string endpoint
        datetime timestamp
        string result
    }
```

### Flyway Migrations

| Migration | Description |
|-----------|-------------|
| `V1__create_roles.sql` | Roles table with enum constraint |
| `V2__create_users.sql` | Users table with indexes on email, phone, national_id |
| `V3__create_user_roles.sql` | Join table with cascading FK |
| `V4__create_accounts.sql` | Accounts with CHECK constraint (`balance >= 0`) |
| `V5__create_transactions.sql` | Transactions with CHECK constraint (`amount > 0`) |
| `V6__create_refresh_tokens.sql` | Refresh tokens with expiry index |
| `V7__create_verification_tokens.sql` | Verification tokens with expiry index |
| `V8__create_otp_codes.sql` | OTP codes with expiry index |
| `V9__create_notifications.sql` | Notifications with status + created_at indexes |
| `V10__create_audit_logs.sql` | Audit logs with user_id, event_type, timestamp indexes |
| `V11__seed_roles.sql` | Seeds ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_CUSTOMER |

All tables use **InnoDB**, **utf8mb4_unicode_ci** charset, and **BINARY(16)** UUID primary keys.

---

## 🛡️ Security

### Filter Chain

```
Request → RateLimitFilter → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → Controller
```

| Filter | Order | Purpose |
|--------|-------|---------|
| `RateLimitFilter` | `HIGHEST_PRECEDENCE` | Bucket4j-based rate limiting per endpoint |
| `JwtAuthenticationFilter` | `HIGHEST_PRECEDENCE + 1` | JWT validation & SecurityContext population |

### Security Features

| Feature | Implementation |
|---------|---------------|
| **Password Encoding** | BCrypt (strength 12) |
| **JWT Access Token** | 15-minute expiry, HMAC-SHA signed |
| **JWT Refresh Token** | 7-day expiry, stored in DB |
| **Token Blacklist** | Redis/cache-based on logout |
| **Account Lockout** | After 5 failed login attempts |
| **CORS** | Configurable allowed origins |
| **Session Policy** | STATELESS (no HTTP sessions) |
| **Method Security** | `@PreAuthorize` with role expressions |

### Role-Based Access

| Endpoint Pattern | ADMIN | EMPLOYEE | CUSTOMER |
|-----------------|-------|----------|----------|
| `/api/v1/auth/**` | Public | Public | Public |
| `GET /api/v1/users/profile` | ✅ | ✅ | ✅ |
| `DELETE /api/v1/users/{id}` | ✅ | ❌ | ❌ |
| `POST /api/v1/accounts` | ✅ | ✅ | ❌ |
| `PUT /api/v1/accounts/{id}/freeze` | ✅ | ❌ | ❌ |
| `POST /api/v1/transactions/transfer` | ✅ | ✅ | ✅ |
| `POST /api/v1/transactions/reverse/{id}` | ✅ | ❌ | ❌ |
| `GET /api/v1/audit` | ✅ | ❌ | ❌ |

---

## 📡 Event-Driven Design

The system uses **Spring's ApplicationEventPublisher** to decouple business logic from side-effects. All 15 events are immutable Java records.

### Events

| Event | Trigger | Listeners |
|-------|---------|-----------|
| `UserRegisteredEvent` | User registration | Notification, Audit |
| `UserLoggedInEvent` | Successful login | Notification, Audit |
| `UserLockedEvent` | Account locked (failed attempts) | Notification |
| `EmailVerifiedEvent` | Email verified | Audit |
| `PasswordChangedEvent` | Password changed | Notification, Audit |
| `PasswordResetEvent` | Password reset | Audit |
| `OtpGeneratedEvent` | OTP created | Notification |
| `AccountCreatedEvent` | Account created | Notification, Audit |
| `AccountFrozenEvent` | Account frozen | Notification, Audit |
| `AccountClosedEvent` | Account closed | Notification, Audit |
| `MoneyDepositedEvent` | Deposit completed | Notification, Audit |
| `MoneyWithdrawnEvent` | Withdrawal completed | Notification, Audit |
| `MoneyTransferredEvent` | Transfer completed | Notification, Audit |
| `TransactionCompletedEvent` | Transaction success | Audit |
| `TransactionFailedEvent` | Transaction failure | Notification, Audit |

### Event Flow

```mermaid
sequenceDiagram
    participant Svc as Service Layer
    participant EB as ApplicationEventPublisher
    participant NL as NotificationListener (@Async)
    participant AL as AuditListener (@Async)

    Svc->>EB: publishEvent(MoneyTransferredEvent)
    EB->>NL: @EventListener(async) → Create notification
    EB->>AL: @EventListener(async) → Create audit log

    Note over NL,AL: Both listeners run asynchronously<br/>and independently from the main transaction
```

> **Design Note:** Both listeners use `@Async @EventListener`, ensuring they execute outside the main transaction and never block the request thread.

---

## ⚡ Caching

### Strategy

| Mode | Implementation |
|------|---------------|
| **Default** | `ConcurrentMapCacheManager` (in-memory, no Redis required) |
| **Redis** | Conditional bean — activates when `RedisConnectionFactory` is present |

### Cache Names & TTLs

| Cache | TTL | Key Pattern |
|-------|-----|-------------|
| `users` | 3600s | `user::{id}` |
| `accounts` | 1800s | `account::{id}`, `account::number::{num}` |
| `roles` | 7200s | `role::{id}`, `role::name::{name}` |
| `otp` | 900s | `otp::{email}::{purpose}` |
| `refreshTokens` | 600s | — |
| `jwtBlacklist` | 3600s | `jwt::blacklist::{token}` |
| `notificationCount` | 300s | `notification::count::{userId}` |

---

## ⏰ Scheduler

Seven scheduled jobs maintain data hygiene with bulk query operations:

| Job | Schedule | Action | Retention |
|-----|----------|--------|-----------|
| `ExpiredOtpCleanupJob` | 3:00 AM daily | Delete expired OTPs | 7 days |
| `ExpiredVerificationTokenCleanupJob` | 3:00 AM daily | Delete verified/expired tokens | — |
| `ExpiredRefreshTokenCleanupJob` | 2:00 AM daily | Delete expired refresh tokens | — |
| `OldNotificationCleanupJob` | 4:00 AM daily | Delete old notifications | 90 days |
| `OldAuditCleanupJob` | 4:00 AM daily | Delete old audit logs | 365 days |
| `DailyTransferLimitResetJob` | Midnight | Reset daily transfer amounts | — |
| `CacheCleanupJob` | 1:00 AM daily | Log Redis cache statistics | — |

All jobs use `@Modifying @Query` bulk operations for efficient cleanup.

---

## ⚠️ Error Handling

The `GlobalExceptionHandler` provides consistent error responses across the application.

### Standard Error Response

```json
{
  "success": false,
  "message": "Insufficient balance",
  "status": 400,
  "timestamp": "2026-07-23T14:00:00",
  "path": "/api/v1/transactions/withdraw",
  "errors": {
    "balance": "Available: 500.00, Requested: 1000.00"
  }
}
```

### Exception → HTTP Status Mapping

| Exception | Status | When |
|-----------|--------|------|
| `ValidationException` | 400 | Business rule violation |
| `InsufficientBalanceException` | 400 | Balance too low |
| `InvalidOtpException` | 400 | OTP invalid/expired |
| `TransferLimitExceededException` | 400 | Daily limit exceeded |
| `AccountLockedException` | 403 | Too many failed attempts |
| `ForbiddenException` | 403 | Insufficient permissions |
| `UnauthorizedException` | 401 | Authentication required |
| `InvalidTokenException` | 401 | JWT invalid/expired |
| `EntityNotFoundException` | 404 | Resource not found |
| `DuplicateResourceException` | 409 | Resource already exists |
| `EmailSendingException` | 500 | Email delivery failure |
| `NotificationException` | 500 | Notification failure |

---

## 📬 API Examples

<details>
<summary><b>📝 Register</b></summary>

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "01234567890",
  "nationalId": "12345678901234",
  "password": "SecureP@ss123",
  "confirmPassword": "SecureP@ss123"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Registration successful. Please verify your email.",
  "data": null,
  "timestamp": "2026-07-23T14:00:00"
}
```
</details>

<details>
<summary><b>🔐 Login</b></summary>

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecureP@ss123"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```
</details>

<details>
<summary><b>💸 Transfer</b></summary>

```http
POST /api/v1/transactions/transfer
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "senderAccountNumber": "1234567890",
  "receiverAccountNumber": "0987654321",
  "amount": 500.00,
  "description": "Monthly rent payment"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Transfer successful",
  "data": {
    "id": "a1b2c3d4-...",
    "referenceNumber": "TXN20260723000001",
    "amount": 500.00,
    "type": "TRANSFER",
    "status": "SUCCESS",
    "senderAccountNumber": "1234567890",
    "receiverAccountNumber": "0987654321",
    "description": "Monthly rent payment"
  },
  "timestamp": "2026-07-23T14:00:00"
}
```
</details>

<details>
<summary><b>💰 Deposit</b></summary>

```http
POST /api/v1/transactions/deposit
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "amount": 1000.00,
  "description": "Cash deposit"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Deposit successful",
  "data": {
    "referenceNumber": "TXN20260723000002",
    "amount": 1000.00,
    "type": "DEPOSIT",
    "status": "SUCCESS"
  }
}
```
</details>

<details>
<summary><b>🏧 Withdraw</b></summary>

```http
POST /api/v1/transactions/withdraw
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "amount": 200.00,
  "description": "ATM withdrawal"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Withdrawal successful",
  "data": {
    "referenceNumber": "TXN20260723000003",
    "amount": 200.00,
    "type": "WITHDRAW",
    "status": "SUCCESS"
  }
}
```
</details>

<details>
<summary><b>🔄 Refresh Token</b></summary>

```http
POST /api/v1/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```
</details>

<details>
<summary><b>🔑 Forgot Password</b></summary>

```http
POST /api/v1/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "OTP sent to your email",
  "data": null
}
```
</details>

---

## 🧪 Testing

The project includes **43 unit tests** across three test classes using JUnit 5 and Mockito:

| Test Class | Tests | Coverage |
|-----------|-------|----------|
| `AuthServiceImplTest` | 18 | Registration, Login, Email Verification, OTP, Password Reset, Account Locking |
| `TransactionServiceImplTest` | 13 | Deposit, Withdraw, Transfer, Edge Cases |
| `GlobalExceptionHandlerTest` | 12 | All exception handlers, Validation, Access Denied |

### Run Tests

```bash
# Run all tests
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=AuthServiceImplTest

# Run with coverage
.\mvnw.cmd test jacoco:report
```

---

## 🚀 Performance

| Strategy | Implementation |
|----------|---------------|
| **Optimistic Locking** | `@Version` on `BaseEntity` — all entities benefit |
| **Pessimistic Locking** | `@Lock(PESSIMISTIC_WRITE)` for deposit/withdraw/transfer balance operations |
| **Database Indexes** | On email, phone, national_id, account_number, IBAN, reference_number, token columns |
| **Pagination** | All list endpoints support `page`, `size`, `sort` parameters |
| **Specification API** | Dynamic filtering without loading entire tables |
| **Bulk Operations** | Scheduler jobs use `@Modifying @Query` instead of `findAll()` + `deleteById()` |
| **Caching** | Per-entity TTL caching with Redis/in-memory fallback |
| **Async Events** | `@Async` listeners prevent event processing from blocking request threads |
| **N+1 Prevention** | Fetch joins in repository queries for associated entities |

---

## 🔮 Future Improvements

Features not yet implemented that would enhance the system:

- [ ] **Docker & Docker Compose** — Containerized deployment with MySQL + Redis
- [ ] **Testcontainers** — Integration tests with real database containers
- [ ] **CI/CD Pipeline** — GitHub Actions for automated build, test, and deploy
- [ ] **Two-Factor Authentication (2FA)** — TOTP-based 2FA
- [ ] **WebSocket Notifications** — Real-time push notifications
- [ ] **PDF Statement Generation** — Monthly account statements
- [ ] **Multi-Currency Support** — Exchange rates and currency conversion
- [ ] **Loan Module** — Loan application, approval, and repayment
- [ ] **Scheduled Payments** — Recurring transfers and bill payments
- [ ] **API Versioning** — Header-based versioning for backward compatibility
- [ ] **Observability** — Micrometer metrics + distributed tracing
- [ ] **Database Sharding** — Horizontal scaling for transaction tables

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'feat: add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/):

```
feat:     New feature
fix:      Bug fix
docs:     Documentation changes
style:    Code style changes (formatting, etc.)
refactor: Code refactoring
test:     Adding or updating tests
chore:    Build process or tooling changes
```

### Code Style

- Java 21 features (records, sealed classes, pattern matching)
- Lombok for boilerplate reduction
- MapStruct for DTO mapping
- Interface-based service design
- Custom exceptions extending `BusinessException`

---

## 📄 License

This project is licensed under the MIT License.

```
MIT License

Copyright (c) 2026 Mostafa

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

**Built with ❤️ using Java 21 & Spring Boot**

⭐ Star this repo if you find it useful!

</div>
