package com.mostafa.nexus_bank.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String accountNumber, BigDecimal requested, BigDecimal available) {
        super(String.format("Insufficient balance in account '%s'. Requested: %s, Available: %s",
                accountNumber, requested, available));
    }
}
