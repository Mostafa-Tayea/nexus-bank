package com.mostafa.nexus_bank.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object representing a user")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UserResponse {

    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "User's first name", example = "John")
    @JsonProperty("firstName")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    @JsonProperty("lastName")
    private String lastName;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    @JsonProperty("email")
    private String email;

    @Schema(description = "User's phone number", example = "+201234567890")
    @JsonProperty("phone")
    private String phone;

    @Schema(description = "Whether the user account is enabled", example = "true")
    @JsonProperty("enabled")
    private boolean enabled;

    @Schema(description = "Timestamp when the user was created", example = "2026-01-15T10:30:00")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
