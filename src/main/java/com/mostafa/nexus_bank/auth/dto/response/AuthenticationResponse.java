package com.mostafa.nexus_bank.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mostafa.nexus_bank.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response payload returned after successful authentication")
public class AuthenticationResponse {

    @JsonProperty("accessToken")
    @Schema(description = "The JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @JsonProperty("refreshToken")
    @Schema(description = "The refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @JsonProperty("tokenType")
    @Builder.Default
    @Schema(description = "The type of the token", example = "Bearer")
    private String tokenType = "Bearer";

    @JsonProperty("expiresIn")
    @Schema(description = "The time in seconds until the access token expires", example = "3600")
    private Long expiresIn;

    @JsonProperty("user")
    @Schema(description = "The authenticated user's profile information")
    private UserResponse user;
}
