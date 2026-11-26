package com.banking.bankingSystem.repository;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveUser() {

        // Arrange: Create a new user with unique email.
        User user = new User();

        String uniqueId = UUID.randomUUID().toString();

        user.setEmail("repository-test-" + uniqueId + "@example.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        // Act: Save the user in PostgreSQL.
        User savedUser = userRepository.saveAndFlush(user);

        // Clear Hibernate's first-level cache.
        entityManager.clear();

        // Retrieve the user using its generated ID.
        Optional<User> retrievedUser =
                userRepository.findById(savedUser.getUserId());

        // Assert: Verify the retrieved data.
        assertTrue(retrievedUser.isPresent());

        assertEquals(
                savedUser.getUserId(),
                retrievedUser.get().getUserId()
        );

        assertEquals(
                user.getEmail(),
                retrievedUser.get().getEmail()
        );

        assertEquals(
                UserRole.CUSTOMER,
                retrievedUser.get().getRole()
        );

        assertEquals(
                UserStatus.ACTIVE,
                retrievedUser.get().getStatus()
        );
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {

        // Act: Search for a user ID that should not exist.
        Optional<User> result =
                userRepository.findById(Long.MAX_VALUE);

        // Assert: Repository should return an empty Optional.
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindUserByEmail() {

        // Arrange
        String email = "derived-query-" + UUID.randomUUID() + "@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.saveAndFlush(user);

        // Clear Hibernate persistence context
        entityManager.clear();

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {

        // Arrange
        String email = "missing-" + UUID.randomUUID() + "@example.com";

        // Act
        Optional<User> result = userRepository.findByEmail(email);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindUsersByRoleUsingJpql() {

        // Arrange
        User user = new User();

        user.setEmail(
                "jpql-test-" + UUID.randomUUID() + "@example.com"
        );
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.saveAndFlush(user);

        entityManager.clear();

        // Act
        List<User> customers =
                userRepository.findUsersByRole(UserRole.CUSTOMER);

        // Assert
        assertFalse(customers.isEmpty());

        assertTrue(
                customers.stream().anyMatch(
                        customer -> customer.getEmail().equals(user.getEmail())
                )
        );
    }
}