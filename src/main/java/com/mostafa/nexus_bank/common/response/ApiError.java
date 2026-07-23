package com.mostafa.nexus_bank.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
public class ApiError {

    @JsonProperty("success")
    @Builder.Default
    @Schema(description = "Always false for error responses", example = "false")
    private boolean success = false;

    @JsonProperty("message")
    @Schema(description = "Error message", example = "Validation failed")
    private String message;

    @JsonProperty("errors")
    @Schema(description = "Map of field-level validation errors")
    private Map<String, String> errors;

    @JsonProperty("timestamp")
    @Builder.Default
    @Schema(description = "Error timestamp", example = "2026-07-22T10:30:00")
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty("path")
    @Schema(description = "Request path where error occurred", example = "/api/v1/auth/register")
    private String path;

    @JsonProperty("status")
    @Schema(description = "HTTP status code", example = "400")
    private int status;

    public static ApiError of(int status, String message, String path) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .path(path)
                .build();
    }

    public static ApiError of(int status, String message, Map<String, String> errors, String path) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .errors(errors)
                .path(path)
                .build();
    }
}
