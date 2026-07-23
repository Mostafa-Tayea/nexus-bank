package com.mostafa.nexus_bank.audit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Audit log response DTO")
public class AuditResponse {

    @JsonProperty("id")
    @Schema(description = "Unique identifier of the audit log entry", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @JsonProperty("eventType")
    @Schema(description = "Type of the audit event", example = "AUTHENTICATION")
    private String eventType;

    @JsonProperty("action")
    @Schema(description = "Action performed", example = "LOGIN")
    private String action;

    @JsonProperty("username")
    @Schema(description = "Username of the user who performed the action", example = "john_doe")
    private String username;

    @JsonProperty("userId")
    @Schema(description = "UUID of the user who performed the action", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID userId;

    @JsonProperty("ipAddress")
    @Schema(description = "IP address from which the action was performed", example = "192.168.1.100")
    private String ipAddress;

    @JsonProperty("device")
    @Schema(description = "Device information used for the action", example = "Chrome/120.0.0.0")
    private String device;

    @JsonProperty("httpMethod")
    @Schema(description = "HTTP method used", example = "POST")
    private String httpMethod;

    @JsonProperty("endpoint")
    @Schema(description = "API endpoint accessed", example = "/api/v1/auth/login")
    private String endpoint;

    @JsonProperty("requestId")
    @Schema(description = "Unique request identifier", example = "req-abc123")
    private String requestId;

    @JsonProperty("referenceNumber")
    @Schema(description = "Reference number for the transaction", example = "REF-2024-001")
    private String referenceNumber;

    @JsonProperty("timestamp")
    @Schema(description = "Timestamp when the audit event occurred")
    private LocalDateTime timestamp;

    @JsonProperty("result")
    @Schema(description = "Result of the action", example = "SUCCESS")
    private String result;

    @JsonProperty("details")
    @Schema(description = "Additional details about the audit event", example = "Login successful")
    private String details;
}
