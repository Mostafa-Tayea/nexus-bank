package com.mostafa.nexus_bank.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserLoggedInEvent(UUID userId, String email, String ipAddress, String device, LocalDateTime loginTime) {
}
