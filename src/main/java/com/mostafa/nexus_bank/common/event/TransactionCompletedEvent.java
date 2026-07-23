package com.mostafa.nexus_bank.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCompletedEvent(UUID transactionId, String referenceNumber, BigDecimal amount, String type, UUID userId) {
}
