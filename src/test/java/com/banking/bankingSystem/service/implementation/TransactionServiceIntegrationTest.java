
package com.banking.bankingSystem.service.implementation;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.entity.Customer;
import com.banking.bankingSystem.entity.Transaction;
import com.banking.bankingSystem.entity.User;

import com.banking.bankingSystem.enums.AccountStatus;
import com.banking.bankingSystem.enums.AccountType;
import com.banking.bankingSystem.enums.UserRole;
import com.banking.bankingSystem.enums.UserStatus;

import com.banking.bankingSystem.repository.AccountRepository;
import com.banking.bankingSystem.repository.CustomerRepository;
import com.banking.bankingSystem.repository.TransactionAuditRepository;
import com.banking.bankingSystem.repository.TransactionRepository;
import com.banking.bankingSystem.repository.UserRepository;

import com.banking.bankingSystem.service.TransactionService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionAuditRepository transactionAuditRepository;

    @Autowired
    private EntityManager entityManager;


    @Test
    void deposit_shouldPersistBalanceTransactionAndAudit() {

        // Generate unique values for every test execution.
        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "");


        // --------------------------------------------------
        // ARRANGE: Create and persist User
        // --------------------------------------------------

        User user = new User();

        user.setEmail(uniqueId + "@integration.test");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.saveAndFlush(user);


        // --------------------------------------------------
        // ARRANGE: Create and persist Customer
        // --------------------------------------------------

        Customer customer = new Customer();

        customer.setUser(user);
        customer.setFullName("Integration Test Customer");
        customer.setPhoneNumber("9" + uniqueId.substring(0, 9));

        customer = customerRepository.saveAndFlush(customer);


        // --------------------------------------------------
        // ARRANGE: Create and persist Account
        // --------------------------------------------------

        Account account = new Account();

        account.setAccountNumber("INT" + uniqueId.substring(0, 17));
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(AccountStatus.ACTIVE);

        account = accountRepository.saveAndFlush(account);

        Long accountId = account.getAccountId();


        // --------------------------------------------------
        // ACT: Execute Deposit
        // --------------------------------------------------

        Transaction transaction = transactionService.deposit(
                accountId,
                new BigDecimal("50.00")
        );


        // --------------------------------------------------
        // Clear Persistence Context
        // --------------------------------------------------

        // The service transaction has committed after deposit()
        // returns. Clear managed entities to verify fresh DB reads.

        entityManager.clear();


        // --------------------------------------------------
        // ASSERT: Verify Account Balance
        // --------------------------------------------------

        Account updatedAccount = accountRepository.findById(accountId)
                .orElseThrow();

        assertThat(updatedAccount.getBalance())
                .isEqualByComparingTo("150.00");


        // --------------------------------------------------
        // ASSERT: Verify Transaction Persistence
        // --------------------------------------------------

        Transaction savedTransaction = transactionRepository
                .findByTransactionId(transaction.getTransactionId())
                .orElseThrow();

        assertThat(savedTransaction.getStatus().name())
                .isEqualTo("SUCCESS");


        // --------------------------------------------------
        // ASSERT: Verify Audit Persistence
        // --------------------------------------------------

        var auditRecords = transactionAuditRepository
                .findByTransaction_Id(savedTransaction.getId());

        assertThat(auditRecords)
                .hasSize(1);

        assertThat(auditRecords.get(0).getResult())
                .isEqualTo("SUCCESS");

        assertThat(auditRecords.get(0).getOperation())
                .isEqualTo("DEPOSIT");
    }


    @Test
    void withdraw_shouldPersistBalanceTransactionAndAudit() {

        // Generate unique test data
        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "");

        // --------------------------------------------------
        // ARRANGE: Create User
        // --------------------------------------------------

        User user = new User();

        user.setEmail(uniqueId + "@integration.test");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.saveAndFlush(user);


        // --------------------------------------------------
        // ARRANGE: Create Customer
        // --------------------------------------------------

        Customer customer = new Customer();

        customer.setUser(user);
        customer.setFullName("Withdrawal Integration Customer");
        customer.setPhoneNumber("9" + uniqueId.substring(0, 9));

        customer = customerRepository.saveAndFlush(customer);


        // --------------------------------------------------
        // ARRANGE: Create Account
        // --------------------------------------------------

        Account account = new Account();

        account.setAccountNumber("INT" + uniqueId.substring(0, 17));
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(AccountStatus.ACTIVE);

        account = accountRepository.saveAndFlush(account);

        Long accountId = account.getAccountId();


        // --------------------------------------------------
        // ACT: Execute Withdrawal
        // --------------------------------------------------

        Transaction transaction = transactionService.withdraw(
                accountId,
                new BigDecimal("40.00")
        );


        // --------------------------------------------------
        // Clear Persistence Context
        // --------------------------------------------------

        entityManager.clear();


        // --------------------------------------------------
        // ASSERT: Verify Account Balance
        // --------------------------------------------------

        Account updatedAccount = accountRepository.findById(accountId)
                .orElseThrow();

        assertThat(updatedAccount.getBalance())
                .isEqualByComparingTo("60.00");


        // --------------------------------------------------
        // ASSERT: Verify Transaction
        // --------------------------------------------------

        Transaction savedTransaction = transactionRepository
                .findByTransactionId(transaction.getTransactionId())
                .orElseThrow();

        assertThat(savedTransaction.getStatus().name())
                .isEqualTo("SUCCESS");


        // --------------------------------------------------
        // ASSERT: Verify Audit
        // --------------------------------------------------

        var auditRecords = transactionAuditRepository
                .findByTransaction_Id(savedTransaction.getId());

        assertThat(auditRecords)
                .hasSize(1);

        assertThat(auditRecords.get(0).getResult())
                .isEqualTo("SUCCESS");

        assertThat(auditRecords.get(0).getOperation())
                .isEqualTo("WITHDRAWAL");
    }


    @Test
    void withdrawWithInsufficientBalance_shouldPersistRejectionWithoutChangingBalance() {

        // Generate unique test data
        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "");

        // --------------------------------------------------
        // ARRANGE: Create User
        // --------------------------------------------------

        User user = new User();

        user.setEmail(uniqueId + "@integration.test");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.saveAndFlush(user);


        // --------------------------------------------------
        // ARRANGE: Create Customer
        // --------------------------------------------------

        Customer customer = new Customer();

        customer.setUser(user);
        customer.setFullName("Insufficient Balance Customer");
        customer.setPhoneNumber("9" + uniqueId.substring(0, 9));

        customer = customerRepository.saveAndFlush(customer);


        // --------------------------------------------------
        // ARRANGE: Create Account
        // --------------------------------------------------

        Account account = new Account();

        account.setAccountNumber("INT" + uniqueId.substring(0, 17));
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("20.00"));
        account.setStatus(AccountStatus.ACTIVE);

        account = accountRepository.saveAndFlush(account);

        Long accountId = account.getAccountId();


        // --------------------------------------------------
        // ACT: Attempt Withdrawal
        // --------------------------------------------------

        Transaction transaction = transactionService.withdraw(
                accountId,
                new BigDecimal("50.00")
        );


        // --------------------------------------------------
        // Clear Persistence Context
        // --------------------------------------------------

        entityManager.clear();


        // --------------------------------------------------
        // ASSERT: Balance Must Remain Unchanged
        // --------------------------------------------------

        Account updatedAccount = accountRepository.findById(accountId)
                .orElseThrow();

        assertThat(updatedAccount.getBalance())
                .isEqualByComparingTo("20.00");


        // --------------------------------------------------
        // ASSERT: Transaction Must Be Rejected
        // --------------------------------------------------

        Transaction savedTransaction = transactionRepository
                .findByTransactionId(transaction.getTransactionId())
                .orElseThrow();

        assertThat(savedTransaction.getStatus().name())
                .isEqualTo("REJECTED");


        // --------------------------------------------------
        // ASSERT: Rejection Audit Must Be Persisted
        // --------------------------------------------------

        var auditRecords = transactionAuditRepository
                .findByTransaction_Id(savedTransaction.getId());

        assertThat(auditRecords)
                .hasSize(1);

        assertThat(auditRecords.get(0).getResult())
                .isEqualTo("REJECTED");

        assertThat(auditRecords.get(0).getOperation())
                .isEqualTo("WITHDRAWAL");
    }


    @Test
    void depositOnInactiveAccount_shouldPersistRejectionWithoutChangingBalance() {

        // Generate unique test data
        String uniqueId = UUID.randomUUID()
                .toString()
                .replace("-", "");

        // --------------------------------------------------
        // ARRANGE: Create User
        // --------------------------------------------------

        User user = new User();

        user.setEmail(uniqueId + "@integration.test");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.saveAndFlush(user);


        // --------------------------------------------------
        // ARRANGE: Create Customer
        // --------------------------------------------------

        Customer customer = new Customer();

        customer.setUser(user);
        customer.setFullName("Inactive Account Customer");
        customer.setPhoneNumber("9" + uniqueId.substring(0, 9));

        customer = customerRepository.saveAndFlush(customer);


        // --------------------------------------------------
        // ARRANGE: Create Inactive Account
        // --------------------------------------------------

        Account account = new Account();

        account.setAccountNumber("INT" + uniqueId.substring(0, 17));
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(AccountStatus.INACTIVE);

        account = accountRepository.saveAndFlush(account);

        Long accountId = account.getAccountId();


        // --------------------------------------------------
        // ACT: Attempt Deposit
        // --------------------------------------------------

        Transaction transaction = transactionService.deposit(
                accountId,
                new BigDecimal("50.00")
        );


        // --------------------------------------------------
        // Clear Persistence Context
        // --------------------------------------------------

        entityManager.clear();


        // --------------------------------------------------
        // ASSERT: Balance Must Remain Unchanged
        // --------------------------------------------------

        Account updatedAccount = accountRepository.findById(accountId)
                .orElseThrow();

        assertThat(updatedAccount.getBalance())
                .isEqualByComparingTo("100.00");


        // --------------------------------------------------
        // ASSERT: Transaction Must Be Rejected
        // --------------------------------------------------

        Transaction savedTransaction = transactionRepository
                .findByTransactionId(transaction.getTransactionId())
                .orElseThrow();

        assertThat(savedTransaction.getStatus().name())
                .isEqualTo("REJECTED");


        // --------------------------------------------------
        // ASSERT: Rejection Audit Must Be Persisted
        // --------------------------------------------------

        var auditRecords = transactionAuditRepository
                .findByTransaction_Id(savedTransaction.getId());

        assertThat(auditRecords)
                .hasSize(1);

        assertThat(auditRecords.get(0).getResult())
                .isEqualTo("REJECTED");

        assertThat(auditRecords.get(0).getOperation())
                .isEqualTo("DEPOSIT");
    }

}