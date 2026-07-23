package com.mostafa.nexus_bank.account.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mostafa.nexus_bank.common.enums.AccountStatus;
import com.mostafa.nexus_bank.common.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Account details response")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class AccountResponse {

    @Schema(description = "Unique identifier of the account")
    @JsonProperty("id")
    private UUID id;

    @Schema(description = "Account number", example = "1234567890")
    @JsonProperty("accountNumber")
    private String accountNumber;

    @Schema(description = "International Bank Account Number", example = "SA0380000000608010167519")
    @JsonProperty("iban")
    private String iban;

    @Schema(description = "Current account balance", example = "1500.00")
    @JsonProperty("balance")
    private BigDecimal balance;

    @Schema(description = "Currency code in ISO 4217 format", example = "USD")
    @JsonProperty("currency")
    private String currency;

    @Schema(description = "Type of the account", example = "SAVINGS")
    @JsonProperty("type")
    private AccountType type;

    @Schema(description = "Current status of the account", example = "ACTIVE")
    @JsonProperty("status")
    private AccountStatus status;

    @Schema(description = "Daily transfer limit", example = "5000.00")
    @JsonProperty("dailyTransferLimit")
    private BigDecimal dailyTransferLimit;

    @Schema(description = "Account creation timestamp")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
