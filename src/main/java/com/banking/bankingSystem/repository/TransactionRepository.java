package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.Transaction;
import com.banking.bankingSystem.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findBySourceAccount_AccountId(Long accountId);

    List<Transaction> findByDestinationAccount_AccountId(Long accountId);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findBySourceAccount_AccountIdOrDestinationAccount_AccountId(
            Long sourceAccountId,
            Long destinationAccountId
    );
}