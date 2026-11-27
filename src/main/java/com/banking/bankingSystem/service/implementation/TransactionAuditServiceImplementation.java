package com.banking.bankingSystem.service.implementation;

import com.banking.bankingSystem.entity.Transaction;
import com.banking.bankingSystem.entity.TransactionAudit;

import com.banking.bankingSystem.repository.TransactionAuditRepository;
import com.banking.bankingSystem.service.TransactionAuditService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionAuditServiceImplementation
        implements TransactionAuditService {

    private final TransactionAuditRepository transactionAuditRepository;

    public TransactionAuditServiceImplementation(
            TransactionAuditRepository transactionAuditRepository
    ) {
        this.transactionAuditRepository = transactionAuditRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordAudit(
            Transaction transaction,
            String eventType,
            String operation,
            String result,
            Long durationMs,
            String message
    ) {

        TransactionAudit audit = new TransactionAudit();

        audit.setTransaction(transaction);
        audit.setEventType(eventType);
        audit.setOperation(operation);
        audit.setResult(result);
        audit.setDurationMs(durationMs);
        audit.setMessage(message);
        audit.setThreadName(Thread.currentThread().getName());

        transactionAuditRepository.save(audit);
    }
}
