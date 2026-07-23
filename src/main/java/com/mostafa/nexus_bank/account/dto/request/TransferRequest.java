package com.mostafa.nexus_bank.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request to transfer funds between accounts")
public record TransferRequest(
        @Schema(description = "Sender account number", example = "1234567890")
        @NotBlank(message = "Sender account number is required")
        String senderAccountNumber,

        @Schema(description = "Receiver account number", example = "0987654321")
        @NotBlank(message = "Receiver account number is required")
        String receiverAccountNumber,

        @Schema(description = "Amount to transfer", example = "200.00")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
        BigDecimal amount,

        @Schema(description = "Optional description for the transfer", example = "Monthly rent payment")
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}
