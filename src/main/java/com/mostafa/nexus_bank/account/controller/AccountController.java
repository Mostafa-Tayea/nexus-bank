package com.mostafa.nexus_bank.account.controller;

import com.mostafa.nexus_bank.account.dto.request.CreateAccountRequest;
import com.mostafa.nexus_bank.account.dto.response.AccountResponse;
import com.mostafa.nexus_bank.account.service.AccountService;
import com.mostafa.nexus_bank.common.response.ApiResponse;
import com.mostafa.nexus_bank.common.response.ApiError;
import com.mostafa.nexus_bank.security.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank account management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Create a new bank account",
            description = "Creates a new bank account for the specified user. Requires ADMIN or EMPLOYEE role.",
            tags = {"Accounts"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "IBAN already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Parameter(description = "User UUID to create account for", required = true)
            @RequestParam UUID userId,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Get account by ID",
            description = "Returns an account by its unique identifier. Requires ADMIN or EMPLOYEE role.",
            tags = {"Accounts"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @Parameter(description = "Account UUID", required = true)
            @PathVariable UUID id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success("Account fetched successfully", response));
    }

    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Operation(
            summary = "Get account by account number",
            description = "Returns an account by its account number. Requires ADMIN or EMPLOYEE role.",
            tags = {"Accounts"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @Parameter(description = "Account number", required = true)
            @PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account fetched successfully", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE') or #userId == authentication.principal.user.id")
    @Operation(
            summary = "Get accounts by user ID",
            description = "Returns all accounts for a specific user. Requires ADMIN, EMPLOYEE role, or being the account owner.",
            tags = {"Accounts"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Accounts fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByUserId(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        List<AccountResponse> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Accounts fetched successfully", response));
    }

    @GetMapping("/my-accounts")
    @Operation(
            summary = "Get my accounts",
            description = "Returns all accounts for the currently authenticated user.",
            tags = {"Accounts"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Accounts fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<AccountResponse> response = accountService.getAccountsByUserId(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Accounts fetched successfully", response));
    }

    @PutMapping("/{id}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Freeze an account",
            description = "Freezes a bank account. Requires ADMIN role.",
            tags = {"Accounts"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account frozen successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> freezeAccount(
            @Parameter(description = "Account UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Reason for freezing", required = false)
            @RequestParam(required = false) String reason) {
        AccountResponse response = accountService.freezeAccount(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Account frozen successfully", response));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Close an account",
            description = "Closes a bank account. Requires ADMIN role.",
            tags = {"Accounts"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account closed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(
            @Parameter(description = "Account UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Reason for closing", required = false)
            @RequestParam(required = false) String reason) {
        AccountResponse response = accountService.closeAccount(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Account closed successfully", response));
    }
}
