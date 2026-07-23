package com.mostafa.nexus_bank.transaction.mapper;

import com.mostafa.nexus_bank.transaction.dto.response.TransactionResponse;
import com.mostafa.nexus_bank.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "referenceNumber", target = "referenceNumber")
    @Mapping(source = "senderAccount.accountNumber", target = "senderAccountNumber")
    @Mapping(source = "receiverAccount.accountNumber", target = "receiverAccountNumber")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "transactionType", target = "transactionType")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "createdAt", target = "createdAt")
    TransactionResponse toResponse(Transaction transaction);
}
