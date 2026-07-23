package com.mostafa.nexus_bank.common.event;

import java.util.UUID;

public record UserLockedEvent(UUID userId, String email) {
}
