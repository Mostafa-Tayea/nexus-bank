package com.mostafa.nexus_bank.account.dto.request;

import com.mostafa.nexus_bank.common.enums.AccountType;
import com.mostafa.nexus_bank.validation.ValidIBAN;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request to create a new bank account")
public record CreateAccountRequest(
        @Schema(description = "Type of the account", example = "SAVINGS")
        @NotNull(message = "Account type is required")
        AccountType type,

        @Schema(description = "Currency code in ISO 4217 format", example = "USD")
        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
        String currency,

        @Schema(description = "Daily transfer limit for the account", example = "5000.00")
        @DecimalMin(value = "0.0", inclusive = false, message = "Daily transfer limit must be greater than zero")
        BigDecimal dailyTransferLimit,

        @Schema(description = "International Bank Account Number", example = "SA0380000000608010167519")
        @NotBlank(message = "IBAN is required")
        @ValidIBAN
        String iban
) {
}
