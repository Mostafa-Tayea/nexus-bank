package com.mostafa.nexus_bank.transaction.controller;

import com.mostafa.nexus_bank.common.enums.TransactionStatus;
import com.mostafa.nexus_bank.common.enums.TransactionType;
import com.mostafa.nexus_bank.common.response.ApiResponse;
import com.mostafa.nexus_bank.common.response.ApiError;
import com.mostafa.nexus_bank.security.service.CustomUserDetails;
import com.mostafa.nexus_bank.transaction.dto.request.DepositRequest;
import com.mostafa.nexus_bank.transaction.dto.request.TransferRequest;
import com.mostafa.nexus_bank.transaction.dto.request.WithdrawRequest;
import com.mostafa.nexus_bank.transaction.dto.response.TransactionPageResponse;
import com.mostafa.nexus_bank.transaction.dto.response.TransactionResponse;
import com.mostafa.nexus_bank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Banking transaction operations")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    @Operation(
            summary = "Deposit funds into an account",
            description = "Deposits the specified amount into the target account. " +
                    "Requires ADMIN or TELLER role.",
            tags = {"Transactions"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Deposit completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or account not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit completed successfully", response));
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    @Operation(
            summary = "Withdraw funds from an account",
            description = "Withdraws the specified amount from the target account. " +
                    "Requires ADMIN or TELLER role.",
            tags = {"Transactions"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Withdrawal completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, insufficient balance, or account not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawRequest request) {
        TransactionResponse response = transactionService.withdraw(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal completed successfully", response));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER', 'CUSTOMER')")
    @Operation(
            summary = "Transfer funds between accounts",
            description = "Transfers the specified amount from the source account to the destination account. " +
                    "Requires ADMIN, TELLER, or CUSTOMER role.",
            tags = {"Transactions"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Transfer completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, insufficient balance, or account not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", response));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reverse a transaction",
            description = "Reverses a previously completed transaction. Requires ADMIN role.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            tags = {"Transactions", "Administration"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction reversed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Transaction cannot be reversed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> reverseTransaction(
            @Parameter(description = "Transaction UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Parameter(description = "Reason for reversal", required = false, example = "Customer requested reversal")
            @RequestParam(required = false) String reason) {
        TransactionResponse response = transactionService.reverseTransaction(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Transaction reversed successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    @Operation(
            summary = "Get transaction by ID",
            description = "Returns a transaction by its unique identifier. Requires ADMIN or TELLER role.",
            tags = {"Transactions"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @Parameter(description = "Transaction UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched successfully", response));
    }

    @GetMapping("/reference/{referenceNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    @Operation(
            summary = "Get transaction by reference number",
            description = "Returns a transaction by its unique reference number. Requires ADMIN or TELLER role.",
            tags = {"Transactions"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReferenceNumber(
            @Parameter(description = "Transaction reference number", required = true, example = "TXN-20260722-001")
            @PathVariable String referenceNumber) {
        TransactionResponse response = transactionService.getTransactionByReferenceNumber(referenceNumber);
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    @Operation(
            summary = "Get transaction history",
            description = "Returns a paginated list of transactions with optional filters. " +
                    "Requires ADMIN or TELLER role.",
            tags = {"Transactions"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transactions fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionPageResponse>> getTransactionHistory(
            @Parameter(description = "Search by reference number or description", required = false)
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by transaction type", required = false, example = "TRANSFER")
            @RequestParam(required = false) TransactionType transactionType,
            @Parameter(description = "Filter by transaction status", required = false, example = "SUCCESS")
            @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Minimum amount filter", required = false, example = "100.00")
            @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount filter", required = false, example = "10000.00")
            @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Start date (ISO 8601)", required = false, example = "2026-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "End date (ISO 8601)", required = false, example = "2026-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(description = "Filter by account UUID", required = false)
            @RequestParam(required = false) UUID accountId,
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

        TransactionPageResponse response = transactionService.getTransactionHistory(
                search, transactionType, status, minAmount, maxAmount, fromDate, toDate, accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", response));
    }

    @GetMapping("/my-transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Get my transaction history",
            description = "Returns the transaction history of the currently authenticated customer. " +
                    "Requires CUSTOMER role.",
            tags = {"Transactions"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transactions fetched successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    public ResponseEntity<ApiResponse<TransactionPageResponse>> getMyTransactions(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Parameter(description = "Search by reference number or description", required = false)
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by transaction type", required = false, example = "TRANSFER")
            @RequestParam(required = false) TransactionType transactionType,
            @Parameter(description = "Filter by transaction status", required = false, example = "SUCCESS")
            @RequestParam(required = false) TransactionStatus status,
            @Parameter(description = "Minimum amount filter", required = false, example = "100.00")
            @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount filter", required = false, example = "10000.00")
            @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Start date (ISO 8601)", required = false, example = "2026-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "End date (ISO 8601)", required = false, example = "2026-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
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
        TransactionPageResponse response = transactionService.getMyTransactions(
                userId, search, transactionType, status, minAmount, maxAmount, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", response));
    }
}
