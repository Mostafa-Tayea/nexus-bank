package com.mostafa.nexus_bank.exception;

import java.math.BigDecimal;

public class TransferLimitExceededException extends BusinessException {

    public TransferLimitExceededException(String message) {
        super(message);
    }

    public TransferLimitExceededException(String accountNumber, BigDecimal limit, BigDecimal requested) {
        super(String.format("Transfer limit exceeded for account '%s'. Daily limit: %s, Requested: %s",
                accountNumber, limit, requested));
    }
}
