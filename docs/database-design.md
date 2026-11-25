
# Database Design Specification

## 1. Overview

The Banking System uses PostgreSQL as its relational database.

The database contains seven tables:

1. `users`
2. `customers`
3. `accounts`
4. `transactions`
5. `transaction_audit`
6. `failed_transactions`
7. `security_events`

---

## 2. Database Table Specifications

### 2.1 USERS

Stores authentication and authorization information.

| Column | PostgreSQL Type | Constraints |
|---|---|---|
| user_id | BIGINT | Primary Key, Identity |
| email | VARCHAR(150) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL |
| role | VARCHAR(20) | NOT NULL |
| status | VARCHAR(20) | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Enums:**
- role: CUSTOMER, ADMIN
- status: ACTIVE, INACTIVE, BLOCKED

**Note:**
- Passwords must be stored as secure hashes, never plaintext.
- Admin users do not require customer profiles.
- Email is unique to prevent duplicate user identities.

### 2.2 CUSTOMERS

Stores customer profile information.

| Column | PostgreSQL Type | Constraints |
|---|---|---|
| customer_id | BIGINT | Primary Key, Identity |
| user_id | BIGINT | Foreign Key, UNIQUE, NOT NULL |
| full_name | VARCHAR(100) | NOT NULL |
| phone_number | VARCHAR(15) | NOT NULL, UNIQUE |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Note:**
- Each customer profile belongs to exactly one user.
- A user can have zero or one customer profile.
- Admin users do not need customer profiles.

### 2.3 ACCOUNTS

Stores bank account information and current balances.

| Column | PostgreSQL Type | Constraints |
|---|---|---|
| account_id | BIGINT | Primary Key, Identity |
| account_number | VARCHAR(20) | NOT NULL, UNIQUE |
| customer_id | BIGINT | Foreign Key, NOT NULL |
| account_type | VARCHAR(20) | NOT NULL |
| balance | NUMERIC(19,2) | NOT NULL, DEFAULT 0, CHECK >= 0 |
| status | VARCHAR(20) | NOT NULL |
| version | BIGINT | NOT NULL, DEFAULT 0 |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Enums:**
- account_type: SAVINGS, CURRENT
- status: ACTIVE, INACTIVE, FROZEN, CLOSED

**Note:**
- `NUMERIC(19,2)` provides exact decimal precision for monetary values.
- Java uses `BigDecimal` for monetary calculations.
- `version` supports optimistic locking through JPA's `@Version`.
- Account balances must never become negative.
- Only active accounts can perform financial operations.

### 2.4 TRANSACTIONS

Stores the primary record of every transaction request.

| Column | PostgreSQL Type | Constraints |
|---|---|---|
| id | BIGINT | Primary Key, Identity |
| transaction_id | VARCHAR(50) | NOT NULL, UNIQUE |
| source_account_id | BIGINT | Foreign Key, Nullable |
| destination_account_id | BIGINT | Foreign Key, Nullable |
| transaction_type | VARCHAR(20) | NOT NULL |
| amount | NUMERIC(19,2) | NOT NULL, CHECK > 0 |
| status | VARCHAR(20) | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL |
| completed_at | TIMESTAMPTZ | Nullable |

**Enums:**
- transaction_type: DEPOSIT, WITHDRAWAL, TRANSFER
- status: PENDING, SUCCESS, FAILED, REJECTED

**Transaction-specific rules:**

| Transaction Type | Source Account | Destination Account |
|---|---|---|
| DEPOSIT | NULL | NOT NULL |
| WITHDRAWAL | NOT NULL | NULL |
| TRANSFER | NOT NULL | NOT NULL |

Additional constraint:
- A transfer cannot have the same source and destination account.

**Note:**
- `id` is the internal database primary key.
- `transaction_id` is the unique external transaction identifier used for idempotency.
- The unique constraint on `transaction_id` prevents duplicate transaction records.
- The transaction status represents the final processing outcome or current processing state.

### 2.5 TRANSACTION_AUDIT

Stores the processing history of transactions.

| Column | PostgreSQL Type | Constraints |
|---|---|---|
| audit_id | BIGINT | Primary Key, Identity |
| transaction_record_id | BIGINT | Foreign Key, NOT NULL |
| event_type | VARCHAR(50) | NOT NULL |
| thread_name | VARCHAR(100) | Nullable |
| operation | VARCHAR(100) | NOT NULL |
| result | VARCHAR(20) | NOT NULL |
| duration_ms | BIGINT | Nullable, CHECK >= 0 |
| message | VARCHAR(255) | Nullable |
| created_at | TIMESTAMPTZ | NOT NULL |

**Note:**
- One transaction can have multiple audit records.
- Audit records capture relevant processing events.
- `transaction_record_id` references `transactions.id`.
- Audit history helps investigate transaction failures and processing behavior.

### 2.6 FAILED_TRANSACTIONS

Stores structured information about unsuccessful transactions.

| Column | PostgreSQL Type | Constraints |
|---|---|---|
| failure_id | BIGINT | Primary Key, Identity |
| transaction_record_id | BIGINT | Foreign Key, UNIQUE, NOT NULL |
| error_category | VARCHAR(50) | NOT NULL |
| failure_reason | VARCHAR(255) | NOT NULL |
| failure_stage | VARCHAR(50) | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL |

**Note:**
- A transaction can have zero or one failure record.
- The unique constraint on `transaction_record_id` prevents duplicate failure records.
- Rejected business operations and unexpected technical failures should have traceable reasons.
- This table complements, rather than replaces, transaction audit records.

### 2.7 SECURITY_EVENTS

Stores basic security-related events.

| Column | PostgreSQL Type | Constraints |
|---|---|---|
| event_id | BIGINT | Primary Key, Identity |
| user_id | BIGINT | Foreign Key, Nullable |
| event_type | VARCHAR(30) | NOT NULL |
| outcome | VARCHAR(20) | NOT NULL |
| ip_address | VARCHAR(45) | Nullable |
| description | VARCHAR(255) | Nullable |
| created_at | TIMESTAMPTZ | NOT NULL |

**Enums:**
- event_type: LOGIN, LOGOUT, UNAUTHORIZED_ACCESS
- outcome: SUCCESS, FAILURE

**Note:**
- `user_id` is nullable because an unauthenticated user may generate a security event.
- Never store passwords, password hashes, authentication tokens, or other credentials in security-event records.
- This table is intended for basic security-event tracking, not advanced security monitoring.

---

## 3. Relationships

| Parent Table | Child Table | Relationship |
|---|---|---|
| USERS | CUSTOMERS | One-to-zero-or-one |
| CUSTOMERS | ACCOUNTS | One-to-many |
| ACCOUNTS | TRANSACTIONS | One-to-many through source account |
| ACCOUNTS | TRANSACTIONS | One-to-many through destination account |
| TRANSACTIONS | TRANSACTION_AUDIT | One-to-many |
| TRANSACTIONS | FAILED_TRANSACTIONS | One-to-zero-or-one |
| USERS | SECURITY_EVENTS | One-to-many |

---

## 4. Database Integrity Rules

The database enforce the following rules:

1. Every table must have a primary key.
2. User email, customer phone number, account number, and transaction identifier must be unique.
3. Foreign keys must maintain referential integrity.
4. Account balances cannot be negative.
5. Transaction amounts must be greater than zero.
6. Transaction-specific source and destination account rules must be enforced.
7. A transfer cannot use the same account as both source and destination.
8. Duplicate transaction identifiers must not create duplicate financial operations.
9. Historical financial records must not be accidentally deleted.

### Referential deletion policy

- Financial records should use RESTRICT or NO ACTION where deletion could destroy transaction history.
- Users should generally be deactivated rather than deleted.
- Security-event user references may use SET NULL if user deletion is explicitly supported.

---

## 5. Indexing Strategy

The following indexes are planned to support frequently executed queries.

| Table | Column(s) | Purpose |
|---|---|---|
| accounts | customer_id | Retrieve accounts belonging to a customer |
| transactions | source_account_id, created_at | Retrieve outgoing transactions |
| transactions | destination_account_id, created_at | Retrieve incoming transactions |
| transactions | status, created_at | Filter transactions by status and date |
| transaction_audit | transaction_record_id, created_at | Retrieve transaction audit history |
| security_events | user_id, created_at | Retrieve security events for a user |

---

## 6. Data Type Decisions

| Requirement | PostgreSQL | Java |
|---|---|---|
| Monetary values | NUMERIC(19,2) | BigDecimal |
| Primary keys | BIGINT IDENTITY | Long |
| Timestamps | TIMESTAMPTZ | Instant |
| Text | VARCHAR | String |
| Boolean values | BOOLEAN | Boolean / boolean |

**Note:**

Floating-point types such as REAL and DOUBLE PRECISION must not be used for financial balances or transaction amounts.

---

## 7. Transaction Processing and Consistency

Financial operations must execute within database transactions.

The processing flow is:

1. Receive the transaction request.
2. Validate transaction details.
3. Check the unique transaction identifier.
4. Begin the database transaction.
5. Lock or safely update the required account records.
6. Validate account status and available balance.
7. Apply the balance changes.
8. Update transaction status and persist audit information.
9. Commit the database transaction.
10. Return success only after the commit succeeds.

If processing fails, monetary changes must be rolled back.

Failure information that must survive rollback will be persisted using an appropriate independent transaction boundary.

---

## 8. Concurrency Control

The project uses database-level concurrency control to prevent simultaneous operations from corrupting account balances.

The account entity includes a version field for optimistic locking support through JPA's `@Version`.

The final transaction-processing implementation must demonstrate safe handling of concurrent withdrawals and transfers.

For transfers involving multiple accounts, account locking must follow a deterministic order to reduce deadlock risk.

---