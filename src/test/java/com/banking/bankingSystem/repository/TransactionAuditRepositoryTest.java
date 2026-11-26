
package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.entity.Customer;
import com.banking.bankingSystem.entity.Transaction;
import com.banking.bankingSystem.entity.TransactionAudit;
import com.banking.bankingSystem.entity.User;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class TransactionAuditRepositoryTest {

    @Autowired
    private TransactionAuditRepository transactionAuditRepository;

    @Autowired
    private EntityManager entityManager;

    private Account createAccount(String accountPrefix, Customer customer) {

        Account account = new Account();

        account.setAccountNumber(
                accountPrefix + UUID.randomUUID().toString().substring(0, 19)
        );
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        entityManager.persist(account);

        return account;
    }

    private Transaction createTransaction() {

        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("audit-test-" + uniqueId + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName("Audit Test Customer");
        customer.setPhoneNumber(
                "9" + String.format(
                        "%09d",
                        Math.floorMod(
                                System.nanoTime(),
                                1_000_000_000L
                        )
                )
        );

        entityManager.persist(customer);

        Account source = createAccount("S", customer);
        Account destination = createAccount("D", customer);

        Transaction transaction = new Transaction();

        transaction.setTransactionId("AUDIT-" + uniqueId);
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setStatus(TransactionStatus.SUCCESS);

        entityManager.persist(transaction);
        entityManager.flush();

        return transaction;
    }

    private TransactionAudit createAudit(
            Transaction transaction,
            String eventType,
            String operation,
            String result
    ) {

        TransactionAudit audit = new TransactionAudit();

        audit.setTransaction(transaction);
        audit.setEventType(eventType);
        audit.setOperation(operation);
        audit.setResult(result);
        audit.setThreadName(Thread.currentThread().getName());
        audit.setDurationMs(25L);
        audit.setMessage("Repository integration test audit");

        return transactionAuditRepository.saveAndFlush(audit);
    }

    @Test
    void shouldFindAuditRecordsByTransactionId() {

        Transaction transaction = createTransaction();

        createAudit(
                transaction,
                "TRANSACTION_CREATED",
                "CREATE_TRANSACTION",
                "SUCCESS"
        );

        Long transactionId = transaction.getId();

        entityManager.clear();

        List<TransactionAudit> results =
                transactionAuditRepository.findByTransaction_Id(
                        transactionId
                );

        assertEquals(1, results.size());
        assertEquals(
                transactionId,
                results.get(0).getTransaction().getId()
        );
    }

    @Test
    void shouldReturnEmptyListWhenTransactionHasNoAuditRecords() {

        Transaction transaction = createTransaction();

        Long transactionId = transaction.getId();

        entityManager.clear();

        List<TransactionAudit> results =
                transactionAuditRepository.findByTransaction_Id(
                        transactionId
                );

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldFindAuditRecordsOrderedByCreatedAtAscending() {

        Transaction transaction = createTransaction();

        createAudit(
                transaction,
                "TRANSACTION_CREATED",
                "CREATE_TRANSACTION",
                "SUCCESS"
        );

        createAudit(
                transaction,
                "ACCOUNT_VALIDATED",
                "VALIDATE_ACCOUNT",
                "SUCCESS"
        );

        createAudit(
                transaction,
                "TRANSACTION_COMPLETED",
                "COMPLETE_TRANSACTION",
                "SUCCESS"
        );

        Long transactionId = transaction.getId();

        entityManager.clear();

        List<TransactionAudit> results =
                transactionAuditRepository
                        .findByTransaction_IdOrderByCreatedAtAsc(
                                transactionId
                        );

        assertEquals(3, results.size());

        for (int i = 1; i < results.size(); i++) {
            assertFalse(
                    results.get(i).getCreatedAt().isBefore(
                            results.get(i - 1).getCreatedAt()
                    )
            );
        }
    }
}