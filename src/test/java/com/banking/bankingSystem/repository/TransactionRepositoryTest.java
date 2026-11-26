package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.entity.Customer;
import com.banking.bankingSystem.entity.Transaction;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer createCustomer(String uniqueId) {

        User user = new User();

        user.setEmail("txn-test-" + uniqueId + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user);

        Customer customer = new Customer();

        customer.setUser(user);
        customer.setFullName("Transaction Test Customer");
        customer.setPhoneNumber(
                String.valueOf(System.nanoTime()).substring(0, 10)
        );

        entityManager.persist(customer);

        return customer;
    }

    private Account createAccount(
            String suffix,
            Customer customer
    ) {

        Account account = new Account();

        account.setAccountNumber("TXN" + suffix);
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        entityManager.persist(account);

        return account;
    }

    private Transaction createTransaction(
            String transactionId,
            Account source,
            Account destination,
            TransactionStatus status
    ) {

        Transaction transaction = new Transaction();

        transaction.setTransactionId(transactionId);
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setStatus(status);

        entityManager.persist(transaction);

        return transaction;
    }

    @Test
    void shouldFindTransactionByTransactionId() {

        String uniqueId = UUID.randomUUID().toString();

        Customer customer = createCustomer(uniqueId);

        Account source = createAccount(
                uniqueId.substring(0, 12),
                customer
        );

        Account destination = createAccount(
                uniqueId.substring(12, 24),
                customer
        );

        String transactionId = "TXN-ID-" + uniqueId;

        createTransaction(
                transactionId,
                source,
                destination,
                TransactionStatus.SUCCESS
        );

        entityManager.flush();
        entityManager.clear();

        Optional<Transaction> result =
                transactionRepository.findByTransactionId(transactionId);

        assertTrue(result.isPresent());

        assertEquals(
                transactionId,
                result.get().getTransactionId()
        );
    }

    @Test
    void shouldFindTransactionsByStatus() {

        String uniqueId = UUID.randomUUID().toString();

        Customer customer = createCustomer(uniqueId);

        Account source = createAccount(
                uniqueId.substring(0, 12),
                customer
        );

        Account destination = createAccount(
                uniqueId.substring(12, 24),
                customer
        );

        String transactionId = "SUCCESS-" + uniqueId;

        createTransaction(
                transactionId,
                source,
                destination,
                TransactionStatus.SUCCESS
        );

        entityManager.flush();
        entityManager.clear();

        List<Transaction> transactions =
                transactionRepository.findByStatus(
                        TransactionStatus.SUCCESS
                );

        assertTrue(
                transactions.stream().anyMatch(
                        transaction ->
                                transaction.getTransactionId()
                                        .equals(transactionId)
                )
        );
    }

    @Test
    void shouldFindTransactionsBySourceAccountId() {

        String uniqueId = UUID.randomUUID().toString();

        Customer customer = createCustomer(uniqueId);

        Account source = createAccount(
                uniqueId.substring(0, 12),
                customer
        );

        Account destination = createAccount(
                uniqueId.substring(12, 24),
                customer
        );

        String transactionId = "SOURCE-" + uniqueId;

        createTransaction(
                transactionId,
                source,
                destination,
                TransactionStatus.SUCCESS
        );

        entityManager.flush();

        Long sourceAccountId = source.getAccountId();

        entityManager.clear();

        List<Transaction> transactions =
                transactionRepository.findBySourceAccount_AccountId(
                        sourceAccountId
                );

        assertTrue(
                transactions.stream().anyMatch(
                        transaction ->
                                transaction.getTransactionId()
                                        .equals(transactionId)
                )
        );
    }
}