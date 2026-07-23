package com.mostafa.nexus_bank.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to deposit funds into an account")
public record DepositRequest(
        @Schema(description = "Target account number", example = "1234567890")
        @NotBlank(message = "Account number is required")
        String accountNumber,

        @Schema(description = "Amount to deposit", example = "100.00")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
        BigDecimal amount
) {
}
