package com.mostafa.nexus_bank.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mostafa.nexus_bank.common.enums.TransactionStatus;
import com.mostafa.nexus_bank.common.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Transaction details response")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class TransactionResponse {

    @Schema(description = "Unique identifier of the transaction")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Unique reference number for the transaction", example = "TXN-20260722-ABC123")
    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @Schema(description = "Sender account number", example = "1234567890")
    @JsonProperty("senderAccountNumber")
    private String senderAccountNumber;

    @Schema(description = "Receiver account number", example = "0987654321")
    @JsonProperty("receiverAccountNumber")
    private String receiverAccountNumber;

    @Schema(description = "Transaction amount", example = "200.00")
    @JsonProperty("amount")
    private BigDecimal amount;

    @Schema(description = "Type of transaction", example = "TRANSFER")
    @JsonProperty("transactionType")
    private TransactionType transactionType;

    @Schema(description = "Current status of the transaction", example = "COMPLETED")
    @JsonProperty("status")
    private TransactionStatus status;

    @Schema(description = "Optional transaction description", example = "Monthly rent payment")
    @JsonProperty("description")
    private String description;

    @Schema(description = "Transaction creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
