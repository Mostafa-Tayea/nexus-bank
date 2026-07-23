package com.mostafa.nexus_bank.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Response payload returned after a successful token refresh")
public class RefreshTokenResponse {

    @JsonProperty("accessToken")
    @Schema(description = "The newly issued JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @JsonProperty("refreshToken")
    @Schema(description = "The newly issued refresh token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @JsonProperty("tokenType")
    @Builder.Default
    @Schema(description = "The type of the token", example = "Bearer")
    private String tokenType = "Bearer";

    @JsonProperty("expiresIn")
    @Schema(description = "The time in seconds until the access token expires", example = "3600")
    private Long expiresIn;
}
