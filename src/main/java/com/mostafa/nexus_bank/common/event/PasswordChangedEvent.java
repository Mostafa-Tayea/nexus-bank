package com.mostafa.nexus_bank.common.event;

import java.util.UUID;

public record PasswordChangedEvent(UUID userId, String email) {
}
