package com.mostafa.nexus_bank.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "Paginated response object containing a list of users")
@Getter
@Builder
@AllArgsConstructor
public class UserPageResponse {

    @Schema(description = "List of users in the current page")
    @JsonProperty("content")
    private List<UserResponse> content;

    @Schema(description = "Current page number (zero-based)", example = "0")
    @JsonProperty("page")
    private int page;

    @Schema(description = "Number of elements per page", example = "10")
    @JsonProperty("size")
    private int size;

    @Schema(description = "Total number of elements across all pages", example = "50")
    @JsonProperty("totalElements")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    @JsonProperty("totalPages")
    private int totalPages;

    @Schema(description = "Whether this is the last page", example = "false")
    @JsonProperty("last")
    private boolean last;
}
