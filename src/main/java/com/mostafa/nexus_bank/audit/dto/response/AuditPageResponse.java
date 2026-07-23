package com.mostafa.nexus_bank.audit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Paginated audit log response DTO")
public class AuditPageResponse {

    @JsonProperty("content")
    @Schema(description = "List of audit log entries in the current page")
    private List<AuditResponse> content;

    @JsonProperty("page")
    @Schema(description = "Current page number", example = "0")
    private int page;

    @JsonProperty("size")
    @Schema(description = "Number of elements per page", example = "20")
    private int size;

    @JsonProperty("totalElements")
    @Schema(description = "Total number of elements across all pages", example = "500")
    private long totalElements;

    @JsonProperty("totalPages")
    @Schema(description = "Total number of pages", example = "25")
    private int totalPages;

    @JsonProperty("last")
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
}
