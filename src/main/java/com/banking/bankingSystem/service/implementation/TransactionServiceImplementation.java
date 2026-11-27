
package com.banking.bankingSystem.service.implementation;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.entity.Transaction;

import com.banking.bankingSystem.enums.AccountStatus;
import com.banking.bankingSystem.enums.TransactionStatus;
import com.banking.bankingSystem.enums.TransactionType;

import com.banking.bankingSystem.repository.TransactionRepository;

import com.banking.bankingSystem.service.AccountService;
import com.banking.bankingSystem.service.TransactionService;
import com.banking.bankingSystem.service.TransactionAuditService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionServiceImplementation implements TransactionService {

    private static final BigDecimal MAX_TRANSACTION_AMOUNT =
            new BigDecimal("99999999999999999.99");

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final TransactionAuditService transactionAuditService;

    public TransactionServiceImplementation(
            AccountService accountService,
            TransactionRepository transactionRepository,
            TransactionAuditService transactionAuditService
    ) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.transactionAuditService = transactionAuditService;
    }

    // --------------------------------------------------
    // DEPOSIT
    // --------------------------------------------------

    @Override
    @Transactional
    public Transaction deposit(Long accountId, BigDecimal amount) {

        long startTime = System.nanoTime();

        // Step 1: Validate amount
        validateAmount(amount);

        // Step 2: Retrieve account
        Account account = accountService.getAccountById(accountId);

        // Step 3: Create transaction
        Transaction transaction = createTransaction(
                account,
                amount,
                TransactionType.DEPOSIT
        );

        // Step 4: Validate account status directly
        // Avoid throwing AccountNotActiveException across
        // another transactional service boundary.

        if (account.getStatus() != AccountStatus.ACTIVE) {

            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setCompletedAt(Instant.now());

            Transaction savedTransaction =
                    transactionRepository.save(transaction);

            recordAudit(
                    savedTransaction,
                    "TRANSACTION_REJECTED",
                    "DEPOSIT",
                    "REJECTED",
                    startTime,
                    "Account is not active."
            );

            return savedTransaction;
        }

        // Step 5: Update account balance
        BigDecimal updatedBalance = account.getBalance().add(amount);

        account.setBalance(updatedBalance);

        // Step 6: Mark transaction successful
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCompletedAt(Instant.now());

        // Step 7: Persist transaction
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // Step 8: Record audit
        recordAudit(
                savedTransaction,
                "TRANSACTION_COMPLETED",
                "DEPOSIT",
                "SUCCESS",
                startTime,
                "Deposit completed successfully"
        );

        return savedTransaction;
    }

    // --------------------------------------------------
    // WITHDRAWAL
    // --------------------------------------------------

    @Override
    @Transactional
    public Transaction withdraw(Long accountId, BigDecimal amount) {

        long startTime = System.nanoTime();

        // Step 1: Validate amount
        validateAmount(amount);

        // Step 2: Retrieve account
        Account account = accountService.getAccountById(accountId);

        // Step 3: Create transaction
        Transaction transaction = createTransaction(
                account,
                amount,
                TransactionType.WITHDRAWAL
        );

        // Step 4: Validate account status directly

        if (account.getStatus() != AccountStatus.ACTIVE) {

            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setCompletedAt(Instant.now());

            Transaction savedTransaction =
                    transactionRepository.save(transaction);

            recordAudit(
                    savedTransaction,
                    "TRANSACTION_REJECTED",
                    "WITHDRAWAL",
                    "REJECTED",
                    startTime,
                    "Account is not active."
            );

            return savedTransaction;
        }

        // Step 5: Check sufficient balance
        if (account.getBalance().compareTo(amount) < 0) {

            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setCompletedAt(Instant.now());

            Transaction savedTransaction =
                    transactionRepository.save(transaction);

            recordAudit(
                    savedTransaction,
                    "TRANSACTION_REJECTED",
                    "WITHDRAWAL",
                    "REJECTED",
                    startTime,
                    "Insufficient account balance"
            );

            return savedTransaction;
        }

        // Step 6: Update account balance
        BigDecimal updatedBalance = account.getBalance().subtract(amount);

        account.setBalance(updatedBalance);

        // Step 7: Mark transaction successful
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCompletedAt(Instant.now());

        // Step 8: Persist transaction
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // Step 9: Record audit
        recordAudit(
                savedTransaction,
                "TRANSACTION_COMPLETED",
                "WITHDRAWAL",
                "SUCCESS",
                startTime,
                "Withdrawal completed successfully"
        );

        return savedTransaction;
    }

    // --------------------------------------------------
    // AMOUNT VALIDATION
    // --------------------------------------------------

    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero."
            );
        }

        if (amount.stripTrailingZeros().scale() > 2) {

            throw new IllegalArgumentException(
                    "Transaction amount cannot have more than two decimal places."
            );
        }

        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {

            throw new IllegalArgumentException(
                    "Transaction amount exceeds the maximum supported limit of "
                            + MAX_TRANSACTION_AMOUNT.toPlainString()
            );
        }
    }

    // --------------------------------------------------
    // TRANSACTION CREATION
    // --------------------------------------------------

    private Transaction createTransaction(
            Account account,
            BigDecimal amount,
            TransactionType transactionType
    ) {

        Transaction transaction = new Transaction();

        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setStatus(TransactionStatus.PENDING);

        if (transactionType == TransactionType.DEPOSIT) {

            transaction.setDestinationAccount(account);

        } else {

            transaction.setSourceAccount(account);
        }

        return transaction;
    }

    // --------------------------------------------------
    // AUDIT RECORDING
    // --------------------------------------------------

    private void recordAudit(
            Transaction transaction,
            String eventType,
            String operation,
            String result,
            long startTime,
            String message
    ) {

        long durationMs =
                (System.nanoTime() - startTime) / 1_000_000;

        transactionAuditService.recordAudit(
                transaction,
                eventType,
                operation,
                result,
                durationMs,
                message
        );
    }
}