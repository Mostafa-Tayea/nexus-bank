package com.mostafa.nexus_bank.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "Paginated transaction list response")
@Getter
@Builder
@AllArgsConstructor
public class TransactionPageResponse {

    @Schema(description = "List of transactions in the current page")
    @JsonProperty("content")
    private List<TransactionResponse> content;

    @Schema(description = "Current page number (zero-based)", example = "0")
    @JsonProperty("page")
    private int page;

    @Schema(description = "Number of elements per page", example = "20")
    @JsonProperty("size")
    private int size;

    @Schema(description = "Total number of elements across all pages", example = "150")
    @JsonProperty("totalElements")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    @JsonProperty("totalPages")
    private int totalPages;

    @Schema(description = "Whether this is the last page", example = "false")
    @JsonProperty("last")
    private boolean last;
}
