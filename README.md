# Multi-Threaded Banking Transaction Processing System

A Java-based banking application designed to process financial transactions safely and efficiently under concurrent workloads.

The system focuses on transaction consistency, concurrency control, secure account management, transaction auditing, and reliable failure handling.

## 1. Project Overview

The Multi-Threaded Banking Transaction Processing System simulates core banking operations while maintaining data integrity during simultaneous transaction requests.

The application supports customer management, bank account management, deposits, withdrawals, and fund transfers.

A bounded thread pool processes multiple transactions concurrently, while database transactions and concurrency-control mechanisms protect account balances from race conditions and inconsistent updates.

## 2. Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java 25 |
| Framework | Spring Boot 4.2.0 |
| Build Tool | Maven |
| Packaging | Jar |
| Backend | Spring MVC |
| Database | PostgreSQL 18 |
| ORM | Spring Data JPA, Hibernate |
| Frontend | Thymeleaf, Bootstrap 5 |
| Security | Spring Security, BCrypt |
| Validation | Jakarta Bean Validation |
| Logging | SLF4J, Logback |
| Concurrency | ExecutorService, ThreadPoolExecutor |
| Testing | JUnit 5, Mockito, AssertJ |
| Monitoring | Spring Boot Actuator |
| Version Control | Git, GitHub |

## 3. Core Features

Planned system capabilities:

- Customer registration and management.
- Bank account creation and management.
- Deposit and withdrawal operations.
- Fund transfers between accounts.
- Account balance and transaction history.
- Concurrent transaction processing.
- Thread-safe balance updates.
- Transaction identification and duplicate request prevention.
- Transaction auditing and failure tracking.
- Secure authentication and authorization.
- Transaction reporting and performance analysis.

## 4. Project Structure

```text
banking-system/
│
├── docs/
│   ├── erd.md
│   └── database-design.md
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/banking/bankingSystem/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── enums/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       ├── application.properties
│   │       └── schema.sql
│   │
│   └── test/
│       └── java/
│           └── com/banking/bankingSystem/
│               ├── BankingSystemApplicationTests.java
│               └── entity/
│                   └── EntityPersistenceTest.java
│
├── .gitignore
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## 5. Prerequisites

Install the following software before running the application:

- JDK 25
- PostgreSQL 18
- Git
- IntelliJ IDEA or another Java IDE

Maven can be executed using the included Maven Wrapper.

Verify the installations:

```powershell
java -version
psql --version
git --version
```

## 6. Database Setup

The application uses PostgreSQL as its relational database.

### Step 1: Create the application user

Log in to PostgreSQL as an administrator and execute:

```sql
CREATE USER banking_app WITH LOGIN PASSWORD 'your_password';
```

### Step 2: Create the database

```sql
CREATE DATABASE banking_system OWNER banking_app;
```

### Step 3: Configure schema ownership

Connect to the database:

```sql
\c banking_system
```

Execute:

```sql
ALTER SCHEMA public OWNER TO banking_app;
```

### Step 4: Configure environment variables

Open PowerShell in the project directory and configure:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/banking_system"
$env:DB_USERNAME="banking_app"
$env:DB_PASSWORD="your_password"
```

Replace `your_password` with the actual password assigned to `banking_app`.

These variables apply to the current PowerShell session.

Never commit actual database credentials to the repository.

## 7. Application Configuration

Database connection properties are maintained in:

```text
src/main/resources/application.properties
```

The application reads database configuration through environment variables.

Hibernate schema generation is disabled. Database schema management will be handled through SQL scripts.

## 8. Database Architecture

The database contains seven tables:

| Table | Responsibility |
|---|---|
| users | Authentication and authorization information |
| customers | Customer profile information |
| accounts | Bank account details and current balances |
| transactions | Primary financial transaction records |
| transaction_audit | Transaction processing history |
| failed_transactions | Structured failure information |
| security_events | Basic security-related events |

### Entity Relationships

- USERS → CUSTOMERS: One-to-zero-or-one.
- CUSTOMERS → ACCOUNTS: One-to-many.
- ACCOUNTS → TRANSACTIONS: One-to-many through source and destination account references.
- TRANSACTIONS → TRANSACTION_AUDIT: One-to-many.
- TRANSACTIONS → FAILED_TRANSACTIONS: One-to-zero-or-one.
- USERS → SECURITY_EVENTS: One-to-many.

### Database Design Documentation

Detailed database design documents are maintained in the `docs/` directory.

| File | Responsibility |
|---|---|
| docs/erd.md | Entity Relationship Diagram and table relationships |
| docs/database-design.md | Table specifications, data types, constraints, indexes, and database design decisions |

## 9. Important Database Design Decisions

### Monetary Precision

| Layer | Data Type |
|---|---|
| PostgreSQL | NUMERIC(19,2) |
| Java | BigDecimal |

Floating-point types such as REAL and DOUBLE PRECISION must not be used for financial balances or transaction amounts.

### Transaction Identification

- `transactions.id` is the internal database primary key.
- `transactions.transaction_id` is the externally supplied unique transaction identifier.
- The unique constraint on `transaction_id` supports idempotent transaction processing.

### Transaction Auditing

Transaction records and transaction audit records are maintained separately.

- `transactions` stores the primary financial operation.
- `transaction_audit` stores multiple processing events associated with that operation.
- `failed_transactions` stores structured failure information.

Application diagnostic logs are maintained separately using SLF4J and Logback.

### Concurrency Control

The project uses optimistic locking through JPA's `@Version` annotation.

The `accounts` table contains a `version` column to detect conflicting concurrent updates.

When a version mismatch occurs, the application will handle the conflict through controlled retry logic and revalidation of business rules.

Fund transfers must execute atomically. If either account update fails, the entire database transaction must roll back.

## 10. Running the Application

Navigate to the project root:

```powershell
cd D:\bankingSystem\banking-system
```

Run the application:

```powershell
mvn spring-boot:run
```

Alternatively, use the Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Once the application starts, access:

```text
http://localhost:8080
```

## 11. Running Tests

Execute the complete test suite:

```powershell
mvn clean test
```

Using Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

## 12. Database Scripts

| File | Responsibility | Status |
|---|---|---|
| `src/main/resources/schema.sql` | Database tables, relationships, constraints, and indexes | Completed |
| `database/seed.sql` | Initial and sample database records | Planned |
| `database/reports.sql` | Reporting queries and transaction analysis | Planned |


## 13. Concurrency and Transaction Safety

The system will use:

- A bounded ThreadPoolExecutor for controlled concurrent execution.
- Database transactions for atomic financial operations.
- Optimistic locking through JPA `@Version`.
- Controlled retry handling for optimistic locking conflicts.
- Deterministic account access ordering during fund transfers.
- Unique transaction identifiers for duplicate request prevention.

The system must prevent invalid balances and ensure that concurrent withdrawals cannot collectively withdraw more money than an account holds.

## 14. Logging and Auditing

The application will maintain separate records for:

- Application execution logs.
- Security-related events.
- Financial transaction history.
- Transaction audit records.
- Failed transaction details.

Sensitive information such as passwords, password hashes, and authentication tokens must never appear in application logs.

## 15. Performance Testing

The system will be evaluated under different concurrent workloads.

Planned worker-thread configurations:

- 1 worker thread
- 5 worker threads
- 10 worker threads
- 20 worker threads

The performance evaluation will use at least 50,000 transactions and measure:

- Total execution time.
- Successful transactions.
- Failed transactions.
- Transactions per second (TPS).
- Concurrency-related bottlenecks.

## 16. Development Status

**Current Phase:** Day 2 — Database Design and Project Foundation.

### Completed

- Spring Boot project initialization.
- PostgreSQL database setup and connectivity.
- Git repository initialization and GitHub integration.
- Database architecture finalized with seven tables.
- Entity Relationship Diagram documentation created.
- Database design specification created.
- PostgreSQL schema created and executed.
- Seven JPA entity classes implemented.
- Entity relationships and enum mappings implemented.
- Optimistic locking configured through JPA `@Version`.
- Hibernate schema validation successful.
- Entity persistence test successful.

### Next Phase

- Day 3 — Repository Layer and Database Access.
- Spring Data JPA repository interfaces.
- Repository query methods.
- Database retrieval and persistence operations.