package com.mostafa.nexus_bank.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Response payload for error details")
public class ErrorResponse {

    @Schema(description = "The HTTP status code", example = "400")
    private int status;

    @Schema(description = "A short description of the error", example = "Bad Request")
    private String error;

    @Schema(description = "A detailed error message", example = "Email is already registered")
    private String message;

    @Schema(description = "The timestamp when the error occurred", example = "2026-07-22T10:30:00Z")
    private String timestamp;
}
