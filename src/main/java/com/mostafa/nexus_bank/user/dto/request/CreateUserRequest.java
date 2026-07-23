package com.mostafa.nexus_bank.user.dto.request;

import com.mostafa.nexus_bank.common.enums.RoleType;
import com.mostafa.nexus_bank.validation.ValidNationalId;
import com.mostafa.nexus_bank.validation.ValidPassword;
import com.mostafa.nexus_bank.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Request object for creating a new user")
public record CreateUserRequest(
        @Schema(description = "User's first name", example = "John", maxLength = 50)
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @Schema(description = "User's last name", example = "Doe", maxLength = 50)
        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @Schema(description = "User's email address", example = "john.doe@example.com", maxLength = 100)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Schema(description = "User's phone number", example = "+201234567890")
        @NotBlank(message = "Phone number is required")
        @ValidPhoneNumber
        String phone,

        @Schema(description = "User's national ID number", example = "29901011234567")
        @NotBlank(message = "National ID is required")
        @ValidNationalId
        String nationalId,

        @Schema(description = "User's password", example = "P@ssw0rd123", minLength = 8)
        @NotBlank(message = "Password is required")
        @ValidPassword
        String password,

        @Schema(description = "Set of roles assigned to the user", example = "[\"ROLE_USER\"]")
        @NotNull(message = "Roles are required")
        @Size(min = 1, message = "At least one role is required")
        Set<RoleType> roles
) {
}
