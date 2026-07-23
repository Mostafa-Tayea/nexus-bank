package com.mostafa.nexus_bank.common.event;

import java.util.UUID;

public record AccountClosedEvent(UUID accountId, String accountNumber, UUID userId, String reason) {
}
