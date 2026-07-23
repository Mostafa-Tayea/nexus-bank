package com.mostafa.nexus_bank.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyDepositedEvent(UUID accountId, String accountNumber, BigDecimal amount, String referenceNumber, UUID userId) {
}
