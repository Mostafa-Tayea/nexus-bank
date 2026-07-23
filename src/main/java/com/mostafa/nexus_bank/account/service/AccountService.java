package com.mostafa.nexus_bank.account.service;

import com.mostafa.nexus_bank.account.dto.request.CreateAccountRequest;
import com.mostafa.nexus_bank.account.dto.response.AccountResponse;
import com.mostafa.nexus_bank.account.entity.Account;
import com.mostafa.nexus_bank.account.repository.AccountRepository;
import com.mostafa.nexus_bank.common.event.AccountCreatedEvent;
import com.mostafa.nexus_bank.common.event.AccountFrozenEvent;
import com.mostafa.nexus_bank.common.enums.AccountStatus;
import com.mostafa.nexus_bank.exception.DuplicateResourceException;
import com.mostafa.nexus_bank.exception.EntityNotFoundException;
import com.mostafa.nexus_bank.user.entity.User;
import com.mostafa.nexus_bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountResponse createAccount(UUID userId, CreateAccountRequest request) {
        log.debug("Creating account for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", userId));

        if (accountRepository.existsByIban(request.iban())) {
            throw new DuplicateResourceException("Account", "IBAN", request.iban());
        }

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .iban(request.iban())
                .balance(java.math.BigDecimal.ZERO)
                .currency(request.currency())
                .type(request.type())
                .status(AccountStatus.ACTIVE)
                .dailyTransferLimit(request.dailyTransferLimit())
                .dailyTransferredAmount(java.math.BigDecimal.ZERO)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);

        eventPublisher.publishEvent(new AccountCreatedEvent(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getType().name(),
                userId));

        log.info("Account created successfully: {} for user: {}", accountNumber, userId);
        return toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account", "id", accountId));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account", "accountNumber", accountNumber));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AccountResponse freezeAccount(UUID accountId, String reason) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account", "id", accountId));

        account.setStatus(AccountStatus.FROZEN);
        Account saved = accountRepository.save(account);

        eventPublisher.publishEvent(new AccountFrozenEvent(
                saved.getId(), saved.getAccountNumber(), saved.getUser().getId(), reason));

        log.info("Account frozen: {} - Reason: {}", saved.getAccountNumber(), reason);
        return toResponse(saved);
    }

    @Transactional
    public AccountResponse closeAccount(UUID accountId, String reason) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account", "id", accountId));

        account.setStatus(AccountStatus.CLOSED);
        Account saved = accountRepository.save(account);

        eventPublisher.publishEvent(new com.mostafa.nexus_bank.common.event.AccountClosedEvent(
                saved.getId(), saved.getAccountNumber(), saved.getUser().getId(), reason));

        log.info("Account closed: {} - Reason: {}", saved.getAccountNumber(), reason);
        return toResponse(saved);
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("%010d", Math.abs(UUID.randomUUID().getLeastSignificantBits() % 10000000000L));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .iban(account.getIban())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .type(account.getType())
                .status(account.getStatus())
                .dailyTransferLimit(account.getDailyTransferLimit())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
