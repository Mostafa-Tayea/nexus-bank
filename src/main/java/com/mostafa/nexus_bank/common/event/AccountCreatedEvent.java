package com.mostafa.nexus_bank.common.event;

import java.util.UUID;

public record AccountCreatedEvent(UUID accountId, String accountNumber, String accountType, UUID userId) {
}
