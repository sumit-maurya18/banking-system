
# Entity Relationship Diagram

There are 7 database tables.

```mermaid
erDiagram

    USERS ||--o| CUSTOMERS : has
    CUSTOMERS ||--o{ ACCOUNTS : owns

    ACCOUNTS o|--o{ TRANSACTIONS : source_account
    ACCOUNTS o|--o{ TRANSACTIONS : destination_account

    TRANSACTIONS ||--o{ TRANSACTION_AUDIT : generates
    TRANSACTIONS ||--o| FAILED_TRANSACTIONS : records

    USERS o|--o{ SECURITY_EVENTS : generates

    USERS {
        BIGINT user_id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR role
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    CUSTOMERS {
        BIGINT customer_id PK
        BIGINT user_id FK,UK
        VARCHAR full_name
        VARCHAR phone_number UK
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    ACCOUNTS {
        BIGINT account_id PK
        VARCHAR account_number UK
        BIGINT customer_id FK
        VARCHAR account_type
        NUMERIC balance
        VARCHAR status
        BIGINT version
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    TRANSACTIONS {
        BIGINT id PK
        VARCHAR transaction_id UK
        BIGINT source_account_id FK
        BIGINT destination_account_id FK
        VARCHAR transaction_type
        NUMERIC amount
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ completed_at
    }

    TRANSACTION_AUDIT {
        BIGINT audit_id PK
        BIGINT transaction_record_id FK
        VARCHAR event_type
        VARCHAR thread_name
        VARCHAR operation
        VARCHAR result
        BIGINT duration_ms
        VARCHAR message
        TIMESTAMPTZ created_at
    }

    FAILED_TRANSACTIONS {
        BIGINT failure_id PK
        BIGINT transaction_record_id FK,UK
        VARCHAR error_category
        VARCHAR failure_reason
        VARCHAR failure_stage
        TIMESTAMPTZ created_at
    }

    SECURITY_EVENTS {
        BIGINT event_id PK
        BIGINT user_id FK
        VARCHAR event_type
        VARCHAR outcome
        VARCHAR ip_address
        VARCHAR description
        TIMESTAMPTZ created_at
    }
```

## Relationship Notes

1. **USERS → CUSTOMERS:** A user can have zero or one customer profile. Admin users do not require customer profiles.
2. **CUSTOMERS → ACCOUNTS:** A customer can own multiple accounts.
3. **ACCOUNTS → TRANSACTIONS:** An account can participate in multiple transactions as either the source or destination account.
4. **TRANSACTIONS → TRANSACTION_AUDIT:** One transaction can generate multiple audit events.
5. **TRANSACTIONS → FAILED_TRANSACTIONS:** A transaction can have zero or one structured failure record.
6. **USERS → SECURITY_EVENTS:** A user can generate multiple security events. The user reference can be null for unauthenticated events.

## Design Notes

- `transactions.id` is the internal primary key.
- `transactions.transaction_id` is the externally supplied unique identifier used for idempotency.
- `transaction_audit.transaction_record_id` references `transactions.id`, not `transactions.transaction_id`.
- `failed_transactions.transaction_record_id` is unique to allow at most one failure record per transaction.
- `accounts.balance` uses PostgreSQL `NUMERIC` for precise monetary values.
- `accounts.version` supports optimistic locking through JPA's `@Version`.
- Financial transaction history must be preserved; account and transaction relationships should not allow accidental deletion of historical records.
