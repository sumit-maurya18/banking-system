
package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.entity.Customer;
import com.banking.bankingSystem.entity.FailedTransaction;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class FailedTransactionRepositoryTest {

    @Autowired
    private FailedTransactionRepository failedTransactionRepository;

    @Autowired
    private EntityManager entityManager;

    private FailedTransaction createFailedTransaction(
            String errorCategory
    ) {

        String uniqueId = UUID.randomUUID().toString();

        // Create User
        User user = new User();
        user.setEmail("failure-test-" + uniqueId + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user);

        // Create Customer
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName("Failed Transaction Test Customer");
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

        // Create Account
        Account account = new Account();
        account.setAccountNumber("F" + uniqueId.substring(0, 19));
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("10000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        entityManager.persist(account);

        // Create a rejected withdrawal
        Transaction transaction = new Transaction();
        transaction.setTransactionId("FAIL-" + uniqueId);
        transaction.setSourceAccount(account);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("20000.00"));
        transaction.setStatus(TransactionStatus.REJECTED);

        entityManager.persist(transaction);
        entityManager.flush();

        // Create failure details
        FailedTransaction failedTransaction = new FailedTransaction();
        failedTransaction.setTransaction(transaction);
        failedTransaction.setErrorCategory(errorCategory);
        failedTransaction.setFailureReason("Insufficient account balance");
        failedTransaction.setFailureStage("BALANCE_VALIDATION");

        return failedTransactionRepository.saveAndFlush(
                failedTransaction
        );
    }

    @Test
    void shouldFindFailedTransactionsByTransactionId() {

        FailedTransaction saved = createFailedTransaction(
                "BUSINESS_VALIDATION"
        );

        Long transactionId =
                saved.getTransaction().getId();

        entityManager.clear();

        List<FailedTransaction> results =
                failedTransactionRepository.findByTransaction_Id(
                        transactionId
                );

        assertEquals(1, results.size());
        assertEquals(
                transactionId,
                results.get(0).getTransaction().getId()
        );
    }

    @Test
    void shouldFindFailedTransactionsByErrorCategory() {

        String category = "BUSINESS_VALIDATION";

        createFailedTransaction(category);

        entityManager.clear();

        List<FailedTransaction> results =
                failedTransactionRepository.findByErrorCategory(
                        category
                );

        assertFalse(results.isEmpty());

        assertTrue(
                results.stream().allMatch(
                        failure -> category.equals(
                                failure.getErrorCategory()
                        )
                )
        );
    }

    @Test
    void shouldReturnEmptyListWhenErrorCategoryDoesNotExist() {

        List<FailedTransaction> results =
                failedTransactionRepository.findByErrorCategory(
                        "NON_EXISTENT_CATEGORY"
                );

        assertTrue(results.isEmpty());
    }
}