
package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.Customer;
import com.banking.bankingSystem.entity.User;
import com.banking.bankingSystem.enums.UserRole;
import com.banking.bankingSystem.enums.UserStatus;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer createCustomer() {

        String uniqueId = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail("customer-test-" + uniqueId + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        entityManager.persist(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName("Repository Test Customer");
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

        return customer;
    }

    @Test
    void shouldFindCustomerByUserId() {

        Customer customer = createCustomer();

        Long userId = customer.getUser().getUserId();

        entityManager.flush();
        entityManager.clear();

        Optional<Customer> result =
                customerRepository.findByUser_UserId(userId);

        assertTrue(result.isPresent());
        assertEquals(
                userId,
                result.get().getUser().getUserId()
        );
    }

    @Test
    void shouldReturnEmptyWhenUserIdDoesNotExist() {

        Optional<Customer> result =
                customerRepository.findByUser_UserId(Long.MAX_VALUE);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindCustomerByPhoneNumber() {

        Customer customer = createCustomer();

        String phoneNumber = customer.getPhoneNumber();

        entityManager.flush();
        entityManager.clear();

        Optional<Customer> result =
                customerRepository.findByPhoneNumber(phoneNumber);

        assertTrue(result.isPresent());
        assertEquals(
                phoneNumber,
                result.get().getPhoneNumber()
        );
    }

    @Test
    void shouldReturnEmptyWhenPhoneNumberDoesNotExist() {

        Optional<Customer> result =
                customerRepository.findByPhoneNumber("9999999999");

        assertTrue(result.isEmpty());
    }
}