
package com.banking.bankingSystem.service.implementation;

import com.banking.bankingSystem.entity.Account;
import com.banking.bankingSystem.entity.Customer;
import com.banking.bankingSystem.entity.User;

import com.banking.bankingSystem.enums.AccountStatus;

import com.banking.bankingSystem.exceptions.AccountNotFoundException;
import com.banking.bankingSystem.exceptions.AccountNotActiveException;
import com.banking.bankingSystem.exceptions.AccountOwnershipException;

import com.banking.bankingSystem.repository.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplementationTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImplementation accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = mock(Account.class);
    }

    @Test
    void getAccountById_WhenAccountExists_ReturnsAccount() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(account.getAccountId()).thenReturn(1L);

        Account result = accountService.getAccountById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getAccountId());

        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountById_WhenAccountDoesNotExist_ThrowsException() {

        when(accountRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById(99L)
        );
    }

    @Test
    void getAccountByAccountNumber_WhenAccountExists_ReturnsAccount() {

        when(accountRepository.findByAccountNumber("ACC1001"))
                .thenReturn(Optional.of(account));

        when(account.getAccountNumber()).thenReturn("ACC1001");

        Account result =
                accountService.getAccountByAccountNumber("ACC1001");

        assertEquals("ACC1001", result.getAccountNumber());
    }

    @Test
    void getAccountByAccountNumber_WhenAccountDoesNotExist_ThrowsException() {

        when(accountRepository.findByAccountNumber("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountByAccountNumber("INVALID")
        );
    }

    @Test
    void getAccountsByCustomerId_ReturnsCustomerAccounts() {

        when(accountRepository.findByCustomer_CustomerId(10L))
                .thenReturn(List.of(account));

        List<Account> result =
                accountService.getAccountsByCustomerId(10L);

        assertEquals(1, result.size());
        assertEquals(account, result.get(0));

        verify(accountRepository).findByCustomer_CustomerId(10L);
    }

    @Test
    void accountExists_WhenAccountExists_ReturnsTrue() {

        when(accountRepository.existsById(1L)).thenReturn(true);

        assertTrue(accountService.accountExists(1L));
    }

    @Test
    void accountExists_WhenAccountDoesNotExist_ReturnsFalse() {

        when(accountRepository.existsById(99L)).thenReturn(false);

        assertFalse(accountService.accountExists(99L));
    }

    @Test
    void getAccountBalance_ReturnsCorrectBalance() {

        BigDecimal expectedBalance = new BigDecimal("10000.00");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(account.getBalance()).thenReturn(expectedBalance);

        BigDecimal actualBalance = accountService.getAccountBalance(1L);

        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    void validateAccountActive_WhenAccountIsActive_DoesNotThrow() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(account.getStatus()).thenReturn(AccountStatus.ACTIVE);

        assertDoesNotThrow(
                () -> accountService.validateAccountActive(1L)
        );
    }

    @Test
    void validateAccountActive_WhenAccountIsInactive_ThrowsException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(account.getStatus()).thenReturn(AccountStatus.INACTIVE);

        assertThrows(
                AccountNotActiveException.class,
                () -> accountService.validateAccountActive(1L)
        );
    }

    @Test
    void validateAccountActive_WhenAccountIsFrozen_ThrowsException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(account.getStatus()).thenReturn(AccountStatus.FROZEN);

        assertThrows(
                AccountNotActiveException.class,
                () -> accountService.validateAccountActive(1L)
        );
    }

    @Test
    void validateAccountActive_WhenAccountIsClosed_ThrowsException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(account.getStatus()).thenReturn(AccountStatus.CLOSED);

        assertThrows(
                AccountNotActiveException.class,
                () -> accountService.validateAccountActive(1L)
        );
    }

    @Test
    void validateAccountOwnership_WhenUserOwnsAccount_DoesNotThrow() {

        Customer customer = mock(Customer.class);
        User user = mock(User.class);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(account.getCustomer()).thenReturn(customer);
        when(customer.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(100L);

        assertDoesNotThrow(
                () -> accountService.validateAccountOwnership(1L, 100L)
        );
    }

    @Test
    void validateAccountOwnership_WhenDifferentUser_ThrowsException() {

        Customer customer = mock(Customer.class);
        User user = mock(User.class);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(account.getCustomer()).thenReturn(customer);
        when(customer.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(200L);

        assertThrows(
                AccountOwnershipException.class,
                () -> accountService.validateAccountOwnership(1L, 100L)
        );
    }
}