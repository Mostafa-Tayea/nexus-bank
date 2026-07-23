package com.mostafa.nexus_bank.account.mapper;

import com.mostafa.nexus_bank.account.dto.request.CreateAccountRequest;
import com.mostafa.nexus_bank.account.dto.response.AccountResponse;
import com.mostafa.nexus_bank.account.entity.Account;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dailyTransferredAmount", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "sentTransactions", ignore = true)
    @Mapping(target = "receivedTransactions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(CreateAccountRequest request);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "accountNumber", target = "accountNumber")
    @Mapping(source = "iban", target = "iban")
    @Mapping(source = "balance", target = "balance")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "dailyTransferLimit", target = "dailyTransferLimit")
    @Mapping(source = "createdAt", target = "createdAt")
    AccountResponse toResponse(Account account);
}
