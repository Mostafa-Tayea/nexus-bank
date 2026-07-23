package com.mostafa.nexus_bank.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mostafa.nexus_bank.common.enums.NotificationStatus;
import com.mostafa.nexus_bank.common.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Schema(description = "Notification response DTO")
public class NotificationResponse {

    @JsonProperty("id")
    @Schema(description = "Unique identifier of the notification", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID id;

    @JsonProperty("title")
    @Schema(description = "Title of the notification", example = "Account Alert")
    private String title;

    @JsonProperty("message")
    @Schema(description = "Message content of the notification", example = "Your transaction was successful")
    private String message;

    @JsonProperty("type")
    @Schema(description = "Type of the notification")
    private NotificationType type;

    @JsonProperty("status")
    @Schema(description = "Status of the notification")
    private NotificationStatus status;

    @JsonProperty("sentAt")
    @Schema(description = "Timestamp when the notification was sent")
    private LocalDateTime sentAt;

    @JsonProperty("createdAt")
    @Schema(description = "Timestamp when the notification was created")
    private LocalDateTime createdAt;
}
