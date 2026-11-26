package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.entity.Customer;
import com.banking.bankingSystem.entity.User;
import com.banking.bankingSystem.enums.AccountStatus;
import com.banking.bankingSystem.enums.AccountType;
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
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindAccountsByCustomerId() {

        // 1. Create User
        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("account-test-" + uniqueId + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user);

        // 2. Create Customer
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName("Repository Test Customer");
        customer.setPhoneNumber(
                String.valueOf(System.nanoTime()).substring(0, 10)
        );

        entityManager.persist(customer);

        // 3. Create Account
        Account account = new Account();
        account.setAccountNumber(
                "TEST" + uniqueId.replace("-", "").substring(0, 12)
        );
        account.setCustomer(customer);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        entityManager.persist(account);

        // 4. Flush SQL statements
        entityManager.flush();

        Long customerId = customer.getCustomerId();
        Long accountId = account.getAccountId();

        // 5. Clear persistence context
        entityManager.clear();

        // 6. Execute repository query
        List<Account> accounts =
                accountRepository.findByCustomer_CustomerId(customerId);

        // 7. Verify results
        assertEquals(1, accounts.size());

        assertEquals(
                accountId,
                accounts.get(0).getAccountId()
        );

        assertEquals(
                customerId,
                accounts.get(0).getCustomer().getCustomerId()
        );
    }

    @Test
    void shouldReturnEmptyListWhenCustomerHasNoAccounts() {

        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("empty-account-" + uniqueId + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName("Customer Without Accounts");
        customer.setPhoneNumber(
                String.valueOf(System.nanoTime()).substring(0, 10)
        );

        entityManager.persist(customer);
        entityManager.flush();

        List<Account> accounts =
                accountRepository.findByCustomer_CustomerId(
                        customer.getCustomerId()
                );

        assertTrue(accounts.isEmpty());
    }
}