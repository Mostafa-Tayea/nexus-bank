package com.mostafa.nexus_bank.common.event;

import java.util.UUID;

public record AccountFrozenEvent(UUID accountId, String accountNumber, UUID userId, String reason) {
}
