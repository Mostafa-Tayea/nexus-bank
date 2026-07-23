package com.mostafa.nexus_bank.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionFailedEvent(UUID transactionId, String referenceNumber, BigDecimal amount, String type, String reason, UUID userId) {
}
