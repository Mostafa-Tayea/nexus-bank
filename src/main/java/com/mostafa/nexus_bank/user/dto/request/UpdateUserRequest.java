package com.mostafa.nexus_bank.user.dto.request;

import com.mostafa.nexus_bank.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for updating an existing user's profile")
public record UpdateUserRequest(
        @Schema(description = "User's first name", example = "John", maxLength = 50)
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @Schema(description = "User's last name", example = "Doe", maxLength = 50)
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @Schema(description = "User's phone number", example = "+201234567890")
        @ValidPhoneNumber
        String phone
) {
}
