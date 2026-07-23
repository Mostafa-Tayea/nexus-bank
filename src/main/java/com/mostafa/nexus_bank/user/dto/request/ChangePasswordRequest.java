package com.mostafa.nexus_bank.user.dto.request;

import com.mostafa.nexus_bank.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request object for changing user password")
public record ChangePasswordRequest(
        @Schema(description = "The user's current password", example = "OldP@ssw0rd")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "The new password to set", example = "NewP@ssw0rd1", minLength = 8)
        @NotBlank(message = "New password is required")
        @ValidPassword
        String newPassword,

        @Schema(description = "Confirmation of the new password", example = "NewP@ssw0rd1")
        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {
}
