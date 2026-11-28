package com.banking.bankingSystem.exceptions;

public class InvalidTransactionAmountException extends IllegalArgumentException {

    public InvalidTransactionAmountException(String message) {
        super(message);
    }
}