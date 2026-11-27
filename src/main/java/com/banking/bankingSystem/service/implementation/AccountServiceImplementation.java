package com.banking.bankingSystem.service.implementation;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.enums.AccountStatus;

import com.banking.bankingSystem.exceptions.AccountNotFoundException;
import com.banking.bankingSystem.exceptions.AccountNotActiveException;
import com.banking.bankingSystem.exceptions.AccountOwnershipException;

import com.banking.bankingSystem.repository.AccountRepository;
import com.banking.bankingSystem.service.AccountService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AccountServiceImplementation implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImplementation(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account getAccountById(Long accountId) {

        return accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with ID: " + accountId
                        )
                );
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with number: " + accountNumber
                        )
                );
    }

    @Override
    public List<Account> getAccountsByCustomerId(Long customerId) {

        return accountRepository.findByCustomer_CustomerId(customerId);
    }

    @Override
    public boolean accountExists(Long accountId) {

        return accountRepository.existsById(accountId);
    }

    @Override
    public BigDecimal getAccountBalance(Long accountId) {

        return getAccountById(accountId).getBalance();
    }

    @Override
    public void validateAccountActive(Long accountId) {

        Account account = getAccountById(accountId);

        if (account.getStatus() != AccountStatus.ACTIVE) {

            throw new AccountNotActiveException(
                    "Account is not active. Current status: "
                            + account.getStatus()
            );
        }
    }

    @Override
    public void validateAccountActive(Account account) {

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account is not active. Current status: "
                            + account.getStatus()
            );
        }
    }

    @Override
    public void validateAccountOwnership(Long accountId, Long userId) {

        Account account = getAccountById(accountId);

        Long accountOwnerUserId = account.getCustomer()
                .getUser()
                .getUserId();

        if (!Objects.equals(accountOwnerUserId, userId)) {

            throw new AccountOwnershipException(
                    "User is not authorized to access this account."
            );
        }
    }
}