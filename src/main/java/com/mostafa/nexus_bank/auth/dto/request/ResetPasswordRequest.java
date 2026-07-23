package com.mostafa.nexus_bank.auth.dto.request;

import com.mostafa.nexus_bank.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for resetting password using a token")
public record ResetPasswordRequest(
        @Schema(description = "The password reset token sent to the user", example = "reset-token-abc123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Reset token is required")
        String token,

        @Schema(description = "The new password to set", example = "NewP@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password is required")
        @ValidPassword
        String newPassword,

        @Schema(description = "Password confirmation, must match the new password", example = "NewP@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {
}
