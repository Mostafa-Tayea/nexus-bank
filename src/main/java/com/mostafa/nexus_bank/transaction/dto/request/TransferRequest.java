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

@Schema(description = "Request to transfer funds between accounts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @Schema(description = "Source account number", example = "1234567890")
    @NotBlank(message = "Source account number is required")
    @JsonProperty("sourceAccountNumber")
    private String sourceAccountNumber;

    @Schema(description = "Destination account number", example = "0987654321")
    @NotBlank(message = "Destination account number is required")
    @JsonProperty("destinationAccountNumber")
    private String destinationAccountNumber;

    @Schema(description = "Amount to transfer", example = "200.00")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @JsonProperty("amount")
    private BigDecimal amount;

    @Schema(description = "Optional description for the transfer", example = "Monthly rent payment")
    @JsonProperty("description")
    private String description;
}
