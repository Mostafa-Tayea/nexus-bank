package com.mostafa.nexus_bank.transaction.service;

import com.mostafa.nexus_bank.common.enums.TransactionStatus;
import com.mostafa.nexus_bank.common.enums.TransactionType;
import com.mostafa.nexus_bank.transaction.dto.request.DepositRequest;
import com.mostafa.nexus_bank.transaction.dto.request.TransferRequest;
import com.mostafa.nexus_bank.transaction.dto.request.WithdrawRequest;
import com.mostafa.nexus_bank.transaction.dto.response.TransactionPageResponse;
import com.mostafa.nexus_bank.transaction.dto.response.TransactionResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse deposit(DepositRequest request);

    TransactionResponse withdraw(WithdrawRequest request);

    TransactionResponse transfer(TransferRequest request);

    TransactionResponse reverseTransaction(UUID transactionId, String reason);

    TransactionResponse getTransactionById(UUID transactionId);

    TransactionResponse getTransactionByReferenceNumber(String referenceNumber);

    TransactionPageResponse getTransactionHistory(
            String search,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            UUID accountId,
            Pageable pageable
    );

    TransactionPageResponse getMyTransactions(
            UUID userId,
            String search,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );
}
