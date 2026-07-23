package com.mostafa.nexus_bank.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Generic response payload containing a message")
public class MessageResponse {

    @JsonProperty("message")
    @Schema(description = "A descriptive message about the operation result", example = "Operation completed successfully")
    private String message;

    @JsonProperty("token")
    @Schema(description = "Verification token for email verification", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    public static MessageResponse of(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }

    public static MessageResponse of(String message, String token) {
        return MessageResponse.builder()
                .message(message)
                .token(token)
                .build();
    }
}
