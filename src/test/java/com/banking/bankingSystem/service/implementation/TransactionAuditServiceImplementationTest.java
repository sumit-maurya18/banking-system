package com.banking.bankingSystem.service.implementation;

import com.banking.bankingSystem.entity.Transaction;
import com.banking.bankingSystem.entity.TransactionAudit;
import com.banking.bankingSystem.repository.TransactionAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionAuditServiceImplementationTest {

    @Mock
    private TransactionAuditRepository transactionAuditRepository;

    @InjectMocks
    private TransactionAuditServiceImplementation transactionAuditService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
    }

    @Test
    void recordAudit_shouldPopulateAndSaveAuditRecord() {

        transactionAuditService.recordAudit(
                transaction,
                "TRANSACTION_COMPLETED",
                "DEPOSIT",
                "SUCCESS",
                25L,
                "Deposit completed successfully"
        );

        ArgumentCaptor<TransactionAudit> captor =
                ArgumentCaptor.forClass(TransactionAudit.class);

        verify(transactionAuditRepository).save(captor.capture());

        TransactionAudit savedAudit = captor.getValue();

        assertSame(transaction, savedAudit.getTransaction());
        assertEquals("TRANSACTION_COMPLETED", savedAudit.getEventType());
        assertEquals("DEPOSIT", savedAudit.getOperation());
        assertEquals("SUCCESS", savedAudit.getResult());
        assertEquals(25L, savedAudit.getDurationMs());
        assertEquals(
                "Deposit completed successfully",
                savedAudit.getMessage()
        );
        assertEquals(
                Thread.currentThread().getName(),
                savedAudit.getThreadName()
        );
    }

    @Test
    void recordAudit_shouldPropagateRepositoryException() {

        RuntimeException repositoryException =
                new RuntimeException("Database failure");

        when(transactionAuditRepository.save(any(TransactionAudit.class)))
                .thenThrow(repositoryException);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> transactionAuditService.recordAudit(
                        transaction,
                        "TRANSACTION_COMPLETED",
                        "DEPOSIT",
                        "SUCCESS",
                        25L,
                        "Deposit completed successfully"
                )
        );

        assertSame(repositoryException, thrown);

        verify(transactionAuditRepository).save(any(TransactionAudit.class));
    }
}