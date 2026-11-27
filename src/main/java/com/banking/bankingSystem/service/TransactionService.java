package com.banking.bankingSystem.service;

import com.banking.bankingSystem.entity.Transaction;

import java.math.BigDecimal;

public interface TransactionService {

    Transaction deposit(Long accountId, BigDecimal amount);

    Transaction withdraw(Long accountId, BigDecimal amount);
}