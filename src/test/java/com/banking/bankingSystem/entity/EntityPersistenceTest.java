
package com.banking.bankingSystem.entity;

import com.banking.bankingSystem.enums.AccountStatus;
import com.banking.bankingSystem.enums.AccountType;
import com.banking.bankingSystem.enums.TransactionStatus;
import com.banking.bankingSystem.enums.TransactionType;
import com.banking.bankingSystem.enums.UserRole;
import com.banking.bankingSystem.enums.UserStatus;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class EntityPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveAllEntities() {

        String uniqueId = UUID.randomUUID().toString();

        // 1. Create and persist User
        User user = new User();
        user.setEmail("test-" + uniqueId + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user);

        // 2. Create and persist Customer
        Customer customer = new Customer();
        customer.setFullName("Test Customer");
        customer.setPhoneNumber(
                String.valueOf(System.nanoTime()).substring(0, 10)
        );

        user.setCustomer(customer);

        entityManager.persist(customer);

        // 3. Create and persist Account
        Account account = new Account();
        account.setAccountNumber("TEST" + uniqueId.substring(0, 12));
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        entityManager.persist(account);

        // 4. Create and persist successful Transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TEST-TXN-" + uniqueId);
        transaction.setSourceAccount(account);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setStatus(TransactionStatus.SUCCESS);

        entityManager.persist(transaction);

        // 5. Create and persist TransactionAudit
        TransactionAudit audit = new TransactionAudit();
        audit.setTransaction(transaction);
        audit.setEventType("TRANSACTION_COMPLETED");
        audit.setThreadName(Thread.currentThread().getName());
        audit.setOperation("WITHDRAWAL");
        audit.setResult("SUCCESS");
        audit.setDurationMs(25L);
        audit.setMessage("Test withdrawal completed");

        entityManager.persist(audit);

        // 6. Create and persist a rejected Transaction
        Transaction failedTransaction = new Transaction();
        failedTransaction.setTransactionId(
                "TEST-FAIL-" + uniqueId.substring(0, 32)
        );
        failedTransaction.setSourceAccount(account);
        failedTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        failedTransaction.setAmount(new BigDecimal("20000.00"));
        failedTransaction.setStatus(TransactionStatus.REJECTED);

        entityManager.persist(failedTransaction);

        // 7. Create and persist FailedTransaction details
        FailedTransaction failure = new FailedTransaction();
        failure.setTransaction(failedTransaction);
        failure.setErrorCategory("BUSINESS_VALIDATION");
        failure.setFailureReason("Insufficient account balance");
        failure.setFailureStage("BALANCE_VALIDATION");

        entityManager.persist(failure);

        // 8. Create and persist SecurityEvent
        SecurityEvent securityEvent = new SecurityEvent();
        securityEvent.setUser(user);
        securityEvent.setEventType("LOGIN");
        securityEvent.setOutcome("SUCCESS");
        securityEvent.setIpAddress("127.0.0.1");
        securityEvent.setDescription("Test login event");

        entityManager.persist(securityEvent);

        // Execute pending SQL statements
        entityManager.flush();

        // Clear Hibernate's first-level persistence cache
        entityManager.clear();

        // Verify User
        User savedUser = entityManager.find(User.class, user.getUserId());

        assertNotNull(savedUser);
        assertEquals(
                user.getEmail(),
                savedUser.getEmail()
        );

        // Verify Customer
        Customer savedCustomer = entityManager.find(
                Customer.class,
                customer.getCustomerId()
        );

        assertNotNull(savedCustomer);
        assertEquals("Test Customer", savedCustomer.getFullName());
        assertEquals(
                user.getUserId(),
                savedCustomer.getUser().getUserId()
        );

        // Verify Account
        Account savedAccount = entityManager.find(
                Account.class,
                account.getAccountId()
        );

        assertNotNull(savedAccount);
        assertEquals(
                0,
                new BigDecimal("10000.00")
                        .compareTo(savedAccount.getBalance())
        );
        assertEquals(
                customer.getCustomerId(),
                savedAccount.getCustomer().getCustomerId()
        );

        // Verify successful Transaction
        Transaction savedTransaction = entityManager.find(
                Transaction.class,
                transaction.getId()
        );

        assertNotNull(savedTransaction);
        assertEquals(
                transaction.getTransactionId(),
                savedTransaction.getTransactionId()
        );
        assertEquals(
                TransactionStatus.SUCCESS,
                savedTransaction.getStatus()
        );

        // Verify TransactionAudit
        TransactionAudit savedAudit = entityManager.find(
                TransactionAudit.class,
                audit.getAuditId()
        );

        assertNotNull(savedAudit);
        assertEquals(
                "TRANSACTION_COMPLETED",
                savedAudit.getEventType()
        );
        assertEquals(
                transaction.getId(),
                savedAudit.getTransaction().getId()
        );

        // Verify FailedTransaction
        FailedTransaction savedFailure = entityManager.find(
                FailedTransaction.class,
                failure.getFailureId()
        );

        assertNotNull(savedFailure);
        assertEquals(
                "Insufficient account balance",
                savedFailure.getFailureReason()
        );
        assertEquals(
                failedTransaction.getId(),
                savedFailure.getTransaction().getId()
        );

        // Verify SecurityEvent
        SecurityEvent savedSecurityEvent = entityManager.find(
                SecurityEvent.class,
                securityEvent.getEventId()
        );

        assertNotNull(savedSecurityEvent);
        assertEquals("LOGIN", savedSecurityEvent.getEventType());
        assertEquals(
                user.getUserId(),
                savedSecurityEvent.getUser().getUserId()
        );
    }
}