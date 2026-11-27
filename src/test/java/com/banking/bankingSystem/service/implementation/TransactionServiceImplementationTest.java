package com.banking.bankingSystem.service.implementation;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.entity.Transaction;

import com.banking.bankingSystem.enums.AccountStatus;
import com.banking.bankingSystem.enums.TransactionStatus;
import com.banking.bankingSystem.enums.TransactionType;

import com.banking.bankingSystem.repository.TransactionRepository;
import com.banking.bankingSystem.service.AccountService;
import com.banking.bankingSystem.service.TransactionAuditService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplementationTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionAuditService transactionAuditService;

    @Mock
    private Account account;

    @InjectMocks
    private TransactionServiceImplementation transactionService;

    private final Long accountId = 1L;

    // ---------------- TEST HELPERS ----------------

    private void prepareActiveAccount(BigDecimal balance) {

        when(accountService.getAccountById(accountId))
                .thenReturn(account);

        when(account.getStatus())
                .thenReturn(AccountStatus.ACTIVE);

        when(account.getBalance())
                .thenReturn(balance);
    }

    private void prepareTransactionSave() {

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void verifyAudit(
            Transaction transaction,
            String eventType,
            String operation,
            String result,
            String message
    ) {

        verify(transactionAuditService).recordAudit(
                same(transaction),
                eq(eventType),
                eq(operation),
                eq(result),
                anyLong(),
                eq(message)
        );
    }

    // ---------------- DEPOSIT TESTS ----------------

    @Test
    void depositShouldIncreaseBalanceAndSaveSuccessfulTransaction() {

        prepareActiveAccount(new BigDecimal("1000.00"));
        prepareTransactionSave();

        BigDecimal amount = new BigDecimal("500.00");

        Transaction result = transactionService.deposit(accountId, amount);

        verify(account).setBalance(new BigDecimal("1500.00"));

        assertEquals(TransactionType.DEPOSIT, result.getTransactionType());
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(amount, result.getAmount());

        assertSame(account, result.getDestinationAccount());
        assertNull(result.getSourceAccount());

        assertNotNull(result.getTransactionId());
        assertNotNull(result.getCompletedAt());

        verify(transactionRepository).save(result);

        verifyAudit(
                result,
                "TRANSACTION_COMPLETED",
                "DEPOSIT",
                "SUCCESS",
                "Deposit completed successfully"
        );
    }

    @Test
    void depositShouldRejectInactiveAccount() {

        when(accountService.getAccountById(accountId))
                .thenReturn(account);

        when(account.getStatus())
                .thenReturn(AccountStatus.INACTIVE);

        prepareTransactionSave();

        Transaction result = transactionService.deposit(
                accountId,
                new BigDecimal("500.00")
        );

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(TransactionType.DEPOSIT, result.getTransactionType());
        assertNotNull(result.getCompletedAt());

        verify(account, never()).setBalance(any());

        verify(transactionRepository).save(result);

        verifyAudit(
                result,
                "TRANSACTION_REJECTED",
                "DEPOSIT",
                "REJECTED",
                "Account is not active."
        );
    }

    @Test
    void depositShouldAcceptSmallestValidAmount() {

        prepareActiveAccount(new BigDecimal("100.00"));
        prepareTransactionSave();

        Transaction result = transactionService.deposit(
                accountId,
                new BigDecimal("0.01")
        );

        verify(account).setBalance(new BigDecimal("100.01"));

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
    }

    @Test
    void depositShouldAcceptTrailingZerosWithinTwoDecimalPlaces() {

        prepareActiveAccount(new BigDecimal("100.00"));
        prepareTransactionSave();

        BigDecimal amount = new BigDecimal("1.230");

        Transaction result = transactionService.deposit(accountId, amount);

        verify(account).setBalance(new BigDecimal("101.230"));

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(amount, result.getAmount());
    }

    // ---------------- WITHDRAWAL TESTS ----------------

    @Test
    void withdrawShouldDecreaseBalanceAndSaveSuccessfulTransaction() {

        prepareActiveAccount(new BigDecimal("1000.00"));
        prepareTransactionSave();

        BigDecimal amount = new BigDecimal("300.00");

        Transaction result = transactionService.withdraw(accountId, amount);

        verify(account).setBalance(new BigDecimal("700.00"));

        assertEquals(TransactionType.WITHDRAWAL, result.getTransactionType());
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(amount, result.getAmount());

        assertSame(account, result.getSourceAccount());
        assertNull(result.getDestinationAccount());

        assertNotNull(result.getTransactionId());
        assertNotNull(result.getCompletedAt());

        verify(transactionRepository).save(result);

        verifyAudit(
                result,
                "TRANSACTION_COMPLETED",
                "WITHDRAWAL",
                "SUCCESS",
                "Withdrawal completed successfully"
        );
    }

    @Test
    void withdrawShouldRejectInactiveAccount() {

        when(accountService.getAccountById(accountId))
                .thenReturn(account);

        when(account.getStatus())
                .thenReturn(AccountStatus.INACTIVE);

        prepareTransactionSave();

        Transaction result = transactionService.withdraw(
                accountId,
                new BigDecimal("200.00")
        );

        assertEquals(TransactionStatus.REJECTED, result.getStatus());
        assertEquals(TransactionType.WITHDRAWAL, result.getTransactionType());
        assertNotNull(result.getCompletedAt());

        verify(account, never()).setBalance(any());

        verify(transactionRepository).save(result);

        verifyAudit(
                result,
                "TRANSACTION_REJECTED",
                "WITHDRAWAL",
                "REJECTED",
                "Account is not active."
        );
    }

    @Test
    void withdrawShouldRejectInsufficientBalance() {

        prepareActiveAccount(new BigDecimal("100.00"));
        prepareTransactionSave();

        Transaction result = transactionService.withdraw(
                accountId,
                new BigDecimal("500.00")
        );

        assertEquals(TransactionStatus.REJECTED, result.getStatus());

        verify(account, never()).setBalance(any());

        verify(transactionRepository).save(result);

        verifyAudit(
                result,
                "TRANSACTION_REJECTED",
                "WITHDRAWAL",
                "REJECTED",
                "Insufficient account balance"
        );
    }

    @Test
    void withdrawShouldAllowExactBalanceWithdrawal() {

        prepareActiveAccount(new BigDecimal("500.00"));
        prepareTransactionSave();

        Transaction result = transactionService.withdraw(
                accountId,
                new BigDecimal("500.00")
        );

        verify(account).setBalance(new BigDecimal("0.00"));

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());

        verifyAudit(
                result,
                "TRANSACTION_COMPLETED",
                "WITHDRAWAL",
                "SUCCESS",
                "Withdrawal completed successfully"
        );
    }

    @Test
    void withdrawShouldRejectWhenAccountBalanceIsZero() {

        prepareActiveAccount(BigDecimal.ZERO);
        prepareTransactionSave();

        Transaction result = transactionService.withdraw(
                accountId,
                new BigDecimal("0.01")
        );

        assertEquals(TransactionStatus.REJECTED, result.getStatus());

        verify(account, never()).setBalance(any());

        verifyAudit(
                result,
                "TRANSACTION_REJECTED",
                "WITHDRAWAL",
                "REJECTED",
                "Insufficient account balance"
        );
    }

    // ---------------- DEPOSIT VALIDATION TESTS ----------------

    @Test
    void depositShouldRejectNullAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(accountId, null)
        );

        verify(accountService, never()).getAccountById(anyLong());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionAuditService);
    }

    @Test
    void depositShouldRejectZeroAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(accountId, BigDecimal.ZERO)
        );

        verify(accountService, never()).getAccountById(anyLong());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionAuditService);
    }

    @Test
    void depositShouldRejectNegativeAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(
                        accountId,
                        new BigDecimal("-100.00")
                )
        );

        verify(accountService, never()).getAccountById(anyLong());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionAuditService);
    }

    @Test
    void depositShouldRejectMoreThanTwoDecimalPlaces() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.deposit(
                        accountId,
                        new BigDecimal("100.123")
                )
        );

        verify(accountService, never()).getAccountById(anyLong());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionAuditService);
    }

    // ---------------- WITHDRAWAL VALIDATION TESTS ----------------

    @Test
    void withdrawShouldRejectNullAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.withdraw(accountId, null)
        );

        verify(accountService, never()).getAccountById(anyLong());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionAuditService);
    }

    @Test
    void withdrawShouldRejectZeroAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.withdraw(accountId, BigDecimal.ZERO)
        );

        verify(accountService, never()).getAccountById(anyLong());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionAuditService);
    }

    @Test
    void withdrawShouldRejectNegativeAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.withdraw(
                        accountId,
                        new BigDecimal("-100.00")
                )
        );

        verify(accountService, never()).getAccountById(anyLong());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionAuditService);
    }

    @Test
    void withdrawShouldRejectMoreThanTwoDecimalPlaces() {

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.withdraw(
                        accountId,
                        new BigDecimal("100.123")
                )
        );

        verify(accountService, never()).getAccountById(anyLong());
        verify(transactionRepository, never()).save(any());
        verifyNoInteractions(transactionAuditService);
    }

    // ---------------- TRANSACTION ID TESTS ----------------

    @Test
    void eachTransactionShouldHaveUniqueTransactionId() {

        prepareActiveAccount(new BigDecimal("1000.00"));
        prepareTransactionSave();

        Transaction first = transactionService.deposit(
                accountId,
                new BigDecimal("100.00")
        );

        Transaction second = transactionService.deposit(
                accountId,
                new BigDecimal("200.00")
        );

        assertNotEquals(
                first.getTransactionId(),
                second.getTransactionId()
        );
    }

    @Test
    void withdraw_shouldAcceptMaximumSupportedAmountWhenBalanceIsSufficient() {

        BigDecimal amount = new BigDecimal("99999999999999999.99");

        prepareActiveAccount(amount);

        transactionService.withdraw(1L, amount);

        verify(account).setBalance(new BigDecimal("0.00"));
    }
}
