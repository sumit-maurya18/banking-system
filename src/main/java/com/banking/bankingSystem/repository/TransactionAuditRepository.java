package com.banking.bankingSystem.repository;

import com.banking.bankingSystem.entity.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionAuditRepository
        extends JpaRepository<TransactionAudit, Long> {

    List<TransactionAudit> findByTransaction_Id(Long transactionId);

    List<TransactionAudit> findByTransaction_IdOrderByCreatedAtAsc(
            Long transactionId
    );

}