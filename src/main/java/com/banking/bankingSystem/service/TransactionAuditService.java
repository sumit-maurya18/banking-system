package com.banking.bankingSystem.service;

import com.banking.bankingSystem.entity.Transaction;

public interface TransactionAuditService {

    void recordAudit(
            Transaction transaction,
            String eventType,
            String operation,
            String result,
            Long durationMs,
            String message
    );
}