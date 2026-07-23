package com.mostafa.nexus_bank.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyTransferredEvent(
        UUID sourceAccountId,
        String sourceAccountNumber,
        UUID destinationAccountId,
        String destinationAccountNumber,
        BigDecimal amount,
        String referenceNumber,
        UUID sourceUserId,
        UUID destinationUserId
) {
}
