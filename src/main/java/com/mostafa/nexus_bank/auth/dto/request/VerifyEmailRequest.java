package com.mostafa.nexus_bank.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for email verification")
public record VerifyEmailRequest(
        @Schema(description = "The email verification token sent to the user", example = "abc123def456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Verification token is required")
        String token
) {
}
