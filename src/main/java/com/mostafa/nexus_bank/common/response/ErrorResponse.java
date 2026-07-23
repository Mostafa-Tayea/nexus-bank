package com.mostafa.nexus_bank.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonProperty("success")
    @Builder.Default
    private boolean success = false;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errors")
    private List<FieldError> errors;

    @JsonProperty("timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty("path")
    private String path;

    @JsonProperty("status")
    private int status;

    @Getter
    @Builder
    public static class FieldError {

        @JsonProperty("field")
        private String field;

        @JsonProperty("message")
        private String message;

        @JsonProperty("rejectedValue")
        private Object rejectedValue;
    }

    public static ErrorResponse of(int status, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse of(int status, String message, List<FieldError> errors, String path) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .errors(errors)
                .path(path)
                .build();
    }
}
