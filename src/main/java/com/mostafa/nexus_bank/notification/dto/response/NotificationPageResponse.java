package com.mostafa.nexus_bank.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Paginated notification response DTO")
public class NotificationPageResponse {

    @JsonProperty("content")
    @Schema(description = "List of notifications in the current page")
    private List<NotificationResponse> content;

    @JsonProperty("page")
    @Schema(description = "Current page number", example = "0")
    private int page;

    @JsonProperty("size")
    @Schema(description = "Number of elements per page", example = "20")
    private int size;

    @JsonProperty("totalElements")
    @Schema(description = "Total number of elements across all pages", example = "150")
    private long totalElements;

    @JsonProperty("totalPages")
    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    @JsonProperty("last")
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @JsonProperty("unreadCount")
    @Schema(description = "Number of unread notifications", example = "5")
    private long unreadCount;
}
