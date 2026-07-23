package com.mostafa.nexus_bank.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    @JsonProperty("success")
    @Schema(description = "Indicates if the request was successful", example = "true")
    private boolean success;

    @JsonProperty("message")
    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @JsonProperty("data")
    @Schema(description = "Response data payload")
    private T data;

    @Builder.Default
    @JsonProperty("timestamp")
    @Schema(description = "Response timestamp", example = "2026-07-22T10:30:00")
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty("path")
    @Schema(description = "Request path", example = "/api/v1/users")
    private String path;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
