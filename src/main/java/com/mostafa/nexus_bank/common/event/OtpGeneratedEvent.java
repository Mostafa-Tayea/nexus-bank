package com.mostafa.nexus_bank.common.event;

import com.mostafa.nexus_bank.common.enums.NotificationType;
import java.util.UUID;

public record OtpGeneratedEvent(UUID userId, String email, String otpCode, NotificationType notificationType) {
}
