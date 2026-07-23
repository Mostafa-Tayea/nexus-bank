package com.mostafa.nexus_bank.notification.controller;

import com.mostafa.nexus_bank.common.response.ApiResponse;
import com.mostafa.nexus_bank.common.response.ApiError;
import com.mostafa.nexus_bank.notification.dto.response.NotificationPageResponse;
import com.mostafa.nexus_bank.notification.dto.response.NotificationResponse;
import com.mostafa.nexus_bank.notification.entity.Notification;
import com.mostafa.nexus_bank.notification.mapper.NotificationMapper;
import com.mostafa.nexus_bank.notification.service.NotificationService;
import com.mostafa.nexus_bank.security.service.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'EMPLOYEE')")
    @Operation(
            summary = "Get my notifications",
            description = "Returns a paginated list of notifications for the currently authenticated user.",
            tags = {"Notifications"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Notifications fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", required = false, example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", required = false, example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        UUID userId = currentUser.getUser().getId();
        NotificationPageResponse response = notificationService.getMyNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'EMPLOYEE')")
    @Operation(
            summary = "Get notification by ID",
            description = "Returns a specific notification by its ID for the currently authenticated user.",
            tags = {"Notifications"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Notification fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - not your notification",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Notification not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @Parameter(description = "Notification UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UUID userId = currentUser.getUser().getId();
        Notification notification = notificationService.getNotificationById(id, userId);
        NotificationResponse response = notificationMapper.toResponse(notification);
        return ResponseEntity.ok(ApiResponse.success("Notification fetched successfully", response));
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'EMPLOYEE')")
    @Operation(
            summary = "Get unread notifications",
            description = "Returns a paginated list of unread notifications for the currently authenticated user.",
            tags = {"Notifications"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Unread notifications fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getMyUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        UUID userId = currentUser.getUser().getId();
        NotificationPageResponse response = notificationService.getMyUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications fetched successfully", response));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'EMPLOYEE')")
    @Operation(
            summary = "Mark notification as read",
            description = "Marks a specific notification as read for the currently authenticated user.",
            tags = {"Notifications"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Notification marked as read",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - not your notification",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Notification not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @Parameter(description = "Notification UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UUID userId = currentUser.getUser().getId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'EMPLOYEE')")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all notifications as read for the currently authenticated user.",
            tags = {"Notifications"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All notifications marked as read",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UUID userId = currentUser.getUser().getId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'EMPLOYEE')")
    @Operation(
            summary = "Delete a notification",
            description = "Deletes a specific notification for the currently authenticated user.",
            tags = {"Notifications"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Notification deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - not your notification",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Notification not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @Parameter(description = "Notification UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UUID userId = currentUser.getUser().getId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }
}
