package com.mostafa.nexus_bank.common.event;

import java.util.UUID;

public record PasswordResetEvent(UUID userId, String email) {
}
