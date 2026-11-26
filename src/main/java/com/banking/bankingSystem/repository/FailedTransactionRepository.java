package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.FailedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedTransactionRepository
        extends JpaRepository<FailedTransaction, Long> {

    List<FailedTransaction> findByTransaction_Id(Long transactionId);

    List<FailedTransaction> findByErrorCategory(String errorCategory);

}