
package com.banking.bankingSystem.service;

import com.banking.bankingSystem.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    Account getAccountById(Long accountId);

    Account getAccountByAccountNumber(String accountNumber);

    List<Account> getAccountsByCustomerId(Long customerId);

    boolean accountExists(Long accountId);

    BigDecimal getAccountBalance(Long accountId);

    void validateAccountActive(Long accountId);

    void validateAccountActive(Account account);

    void validateAccountOwnership(Long accountId, Long userId);
}