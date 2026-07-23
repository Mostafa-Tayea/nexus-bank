package com.mostafa.nexus_bank.common.event;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String email, String fullName) {
}
