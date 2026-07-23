package com.mostafa.nexus_bank.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to withdraw funds from an account")
public record WithdrawRequest(
        @Schema(description = "Source account number", example = "1234567890")
        @NotBlank(message = "Account number is required")
        String accountNumber,

        @Schema(description = "Amount to withdraw", example = "50.00")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Withdrawal amount must be greater than zero")
        BigDecimal amount
) {
}
