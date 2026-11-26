package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.SecurityEvent;
import com.banking.bankingSystem.entity.User;
import com.banking.bankingSystem.enums.SecurityEventOutcome;
import com.banking.bankingSystem.enums.UserRole;
import com.banking.bankingSystem.enums.UserStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SecurityEventRepositoryTest {

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private UserRepository userRepository;

    // Creates a valid user for security event testing.
    private User createUser() {

        User user = new User();

        user.setEmail(UUID.randomUUID() + "@test.com");
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.saveAndFlush(user);
    }

    // Creates and persists a security event.
    private SecurityEvent createSecurityEvent(
            User user,
            String eventType,
            SecurityEventOutcome outcome
    ) {

        SecurityEvent event = new SecurityEvent();

        event.setUser(user);
        event.setEventType(eventType);
        event.setOutcome(outcome);
        event.setIpAddress("192.168.1.100");
        event.setDescription("Security repository integration test");

        return securityEventRepository.saveAndFlush(event);
    }

    @Test
    void shouldFindSecurityEventsByUserId() {

        User user = createUser();

        SecurityEvent createdEvent = createSecurityEvent(
                user,
                "LOGIN",
                SecurityEventOutcome.SUCCESS
        );

        List<SecurityEvent> events =
                securityEventRepository.findByUser_UserId(user.getUserId());

        assertEquals(1, events.size());
        assertEquals(
                createdEvent.getEventId(),
                events.get(0).getEventId()
        );
    }

    @Test
    void shouldFindSecurityEventsByEventType() {

        User user = createUser();

        SecurityEvent createdEvent = createSecurityEvent(
                user,
                "LOGIN",
                SecurityEventOutcome.SUCCESS
        );

        List<SecurityEvent> events =
                securityEventRepository.findByEventType("LOGIN");

        assertEquals(1, events.size());
        assertEquals(
                createdEvent.getEventId(),
                events.get(0).getEventId()
        );
    }

    @Test
    void shouldFindSecurityEventsByOutcome() {

        User user = createUser();

        createSecurityEvent(
                user,
                "LOGIN",
                SecurityEventOutcome.SUCCESS
        );

        SecurityEvent failedEvent = createSecurityEvent(
                user,
                "LOGIN",
                SecurityEventOutcome.FAILURE
        );

        List<SecurityEvent> events =
                securityEventRepository.findByOutcome(
                        SecurityEventOutcome.FAILURE
                );

        assertEquals(1, events.size());

        assertEquals(
                SecurityEventOutcome.FAILURE,
                events.get(0).getOutcome()
        );

        assertEquals(
                failedEvent.getEventId(),
                events.get(0).getEventId()
        );
    }

    @Test
    void shouldReturnEmptyListWhenEventTypeDoesNotExist() {

        List<SecurityEvent> events =
                securityEventRepository.findByEventType(
                        "NON_EXISTENT_EVENT"
                );

        assertEquals(0, events.size());
    }
}