package com.mostafa.nexus_bank.common.event;

import java.util.UUID;

public record EmailVerifiedEvent(UUID userId, String email) {
}
