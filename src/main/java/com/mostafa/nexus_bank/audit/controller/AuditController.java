package com.mostafa.nexus_bank.audit.controller;

import com.mostafa.nexus_bank.audit.dto.response.AuditPageResponse;
import com.mostafa.nexus_bank.audit.service.AuditService;
import com.mostafa.nexus_bank.common.response.ApiResponse;
import com.mostafa.nexus_bank.common.response.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "System audit log endpoints (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(
            summary = "Get all audit logs",
            description = "Returns a paginated list of all audit logs. Requires ADMIN role.",
            tags = {"Audit", "Administration"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<AuditPageResponse>> getAllAuditLogs(
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", required = false, example = "timestamp")
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", required = false, example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        AuditPageResponse response = auditService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get audit logs by user ID",
            description = "Returns audit logs for a specific user. Requires ADMIN role.",
            tags = {"Audit", "Administration"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<AuditPageResponse>> getAuditLogsByUserId(
            @Parameter(description = "User UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID userId,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        AuditPageResponse response = auditService.getAuditLogsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", response));
    }

    @GetMapping("/event/{eventType}")
    @Operation(
            summary = "Get audit logs by event type",
            description = "Returns audit logs filtered by event type. Requires ADMIN role.",
            tags = {"Audit", "Administration"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<AuditPageResponse>> getAuditLogsByEventType(
            @Parameter(description = "Event type (e.g., USER_CREATED, LOGIN_SUCCESS)", required = true, example = "USER_CREATED")
            @PathVariable String eventType,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        AuditPageResponse response = auditService.getAuditLogsByEventType(eventType, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", response));
    }
}
