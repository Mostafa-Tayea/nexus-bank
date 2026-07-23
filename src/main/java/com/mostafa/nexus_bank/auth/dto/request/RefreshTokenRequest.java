package com.mostafa.nexus_bank.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for refreshing an access token")
public record RefreshTokenRequest(
        @Schema(description = "The refresh token issued during login", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
