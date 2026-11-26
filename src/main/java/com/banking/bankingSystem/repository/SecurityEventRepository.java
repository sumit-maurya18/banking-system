package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import com.banking.bankingSystem.enums.SecurityEventOutcome;

import java.util.List;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findByUser_UserId(Long userId);

    List<SecurityEvent> findByEventType(String eventType);

    List<SecurityEvent> findByOutcome(SecurityEventOutcome outcome);

}