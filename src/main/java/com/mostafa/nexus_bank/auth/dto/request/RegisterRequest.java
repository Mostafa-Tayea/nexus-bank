package com.mostafa.nexus_bank.auth.dto.request;

import com.mostafa.nexus_bank.validation.ValidNationalId;
import com.mostafa.nexus_bank.validation.ValidPassword;
import com.mostafa.nexus_bank.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for user registration")
public record RegisterRequest(
        @Schema(description = "User's first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @Schema(description = "User's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @Schema(description = "User's email address", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Schema(description = "User's phone number", example = "+201012345678", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Phone number is required")
        @ValidPhoneNumber
        String phone,

        @Schema(description = "User's national ID number", example = "29001011234567", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "National ID is required")
        @ValidNationalId
        String nationalId,

        @Schema(description = "User's password", example = "P@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @ValidPassword
        String password,

        @Schema(description = "Password confirmation, must match password", example = "P@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {
}
