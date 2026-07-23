package com.mostafa.nexus_bank.exception;

public class AccountLockedException extends BusinessException {

    public AccountLockedException(String message) {
        super(message);
    }

    public static AccountLockedException forAccount(String accountNumber) {
        return new AccountLockedException(
                String.format("Account '%s' is locked due to too many failed attempts", accountNumber));
    }
}
