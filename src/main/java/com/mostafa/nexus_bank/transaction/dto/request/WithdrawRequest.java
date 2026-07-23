package com.mostafa.nexus_bank.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "Request to withdraw funds from an account")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    @Schema(description = "Source account number", example = "1234567890")
    @NotBlank(message = "Account number is required")
    @JsonProperty("accountNumber")
    private String accountNumber;

    @Schema(description = "Amount to withdraw", example = "50.00")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @JsonProperty("amount")
    private BigDecimal amount;

    @Schema(description = "Optional description for the withdrawal", example = "ATM withdrawal")
    @JsonProperty("description")
    private String description;
}
