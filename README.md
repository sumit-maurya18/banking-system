
# Multi-Threaded Banking Transaction Processing System

A Java-based banking application designed to process financial transactions safely and efficiently under concurrent workloads.

The system focuses on transaction consistency, concurrency control, secure account management, transaction auditing, and reliable failure handling.

## 1. Project Overview

The Multi-Threaded Banking Transaction Processing System is designed to simulate core banking operations while maintaining data integrity during simultaneous transaction requests.

The application supports customer management, bank account management, deposits, withdrawals, and fund transfers.

A bounded thread pool is used to process multiple transactions concurrently, while database transactions and locking mechanisms protect account balances from race conditions and inconsistent updates.

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
├── database/
│   ├── schema.sql
│   ├── seed.sql
│   └── reports.sql
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
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
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

## 8. Running the Application

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

## 9. Running Tests

Execute the complete test suite:

```powershell
mvn clean test
```

Using Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

## 10. Database Scripts

Database-related SQL files are maintained in the root-level `database/` directory.

| File | Responsibility |
|---|---|
| schema.sql | Database tables, relationships, constraints and indexes |
| seed.sql | Initial and sample database records |
| reports.sql | Reporting queries and transaction analysis |

These scripts will be implemented during database development.

## 11. Concurrency and Transaction Safety

Concurrency control is a central part of this project.

The system will use:

- A bounded ThreadPoolExecutor for controlled concurrent execution.
- Database transactions for atomic financial operations.
- Appropriate database locking to prevent race conditions.
- Deterministic account locking during fund transfers.
- Transaction identifiers for traceability and duplicate request prevention.

The system must prevent invalid balances and ensure that concurrent withdrawals cannot collectively withdraw more money than an account holds.

## 12. Logging and Auditing

The application will maintain separate records for:

- Application execution logs.
- Security-related events.
- Financial transaction history.
- Transaction audit records.
- Failed transaction details.

Sensitive information such as passwords must never appear in application logs.

## 13. Performance Testing

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

## 14. Development Status

**Current Phase:** Day 1 — Project Foundation and Environment Setup.
