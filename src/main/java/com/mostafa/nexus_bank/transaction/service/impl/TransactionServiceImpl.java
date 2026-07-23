package com.mostafa.nexus_bank.transaction.service.impl;

import com.mostafa.nexus_bank.account.entity.Account;
import com.mostafa.nexus_bank.account.repository.AccountRepository;
import com.mostafa.nexus_bank.common.enums.AccountStatus;
import com.mostafa.nexus_bank.common.enums.TransactionStatus;
import com.mostafa.nexus_bank.common.enums.TransactionType;
import com.mostafa.nexus_bank.common.event.MoneyDepositedEvent;
import com.mostafa.nexus_bank.common.event.MoneyTransferredEvent;
import com.mostafa.nexus_bank.common.event.MoneyWithdrawnEvent;
import com.mostafa.nexus_bank.common.event.TransactionCompletedEvent;
import com.mostafa.nexus_bank.exception.BusinessException;
import com.mostafa.nexus_bank.exception.EntityNotFoundException;
import com.mostafa.nexus_bank.exception.InsufficientBalanceException;
import com.mostafa.nexus_bank.exception.TransferLimitExceededException;
import com.mostafa.nexus_bank.transaction.dto.request.DepositRequest;
import com.mostafa.nexus_bank.transaction.dto.request.TransferRequest;
import com.mostafa.nexus_bank.transaction.dto.request.WithdrawRequest;
import com.mostafa.nexus_bank.transaction.dto.response.TransactionPageResponse;
import com.mostafa.nexus_bank.transaction.dto.response.TransactionResponse;
import com.mostafa.nexus_bank.transaction.entity.Transaction;
import com.mostafa.nexus_bank.transaction.mapper.TransactionMapper;
import com.mostafa.nexus_bank.transaction.repository.TransactionRepository;
import com.mostafa.nexus_bank.transaction.service.TransactionService;
import com.mostafa.nexus_bank.transaction.specification.TransactionSpecification;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account", "accountNumber", request.getAccountNumber()));
        validateAccountActive(account);

        BigDecimal amount = request.getAmount();

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .senderAccount(account)
                .receiverAccount(account)
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Deposit to account " + account.getAccountNumber())
                .build();

        transaction = transactionRepository.save(transaction);

        eventPublisher.publishEvent(new MoneyDepositedEvent(
                account.getId(), account.getAccountNumber(), amount,
                transaction.getReferenceNumber(), account.getUser().getId()));
        eventPublisher.publishEvent(new TransactionCompletedEvent(
                transaction.getId(), transaction.getReferenceNumber(), amount,
                TransactionType.DEPOSIT.name(), account.getUser().getId()));

        log.info("Deposit successful: {} to account {}", amount, account.getAccountNumber());
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request) {
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account", "accountNumber", request.getAccountNumber()));
        validateAccountActive(account);

        BigDecimal amount = request.getAmount();

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getAccountNumber(), amount, account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .senderAccount(account)
                .receiverAccount(account)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAW)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Withdrawal from account " + account.getAccountNumber())
                .build();

        transaction = transactionRepository.save(transaction);

        eventPublisher.publishEvent(new MoneyWithdrawnEvent(
                account.getId(), account.getAccountNumber(), amount,
                transaction.getReferenceNumber(), account.getUser().getId()));
        eventPublisher.publishEvent(new TransactionCompletedEvent(
                transaction.getId(), transaction.getReferenceNumber(), amount,
                TransactionType.WITHDRAW.name(), account.getUser().getId()));

        log.info("Withdrawal successful: {} from account {}", amount, account.getAccountNumber());
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new BusinessException("Cannot transfer to the same account");
        }

        Account sourceAccount = accountRepository.findByAccountNumberForUpdate(request.getSourceAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account", "accountNumber", request.getSourceAccountNumber()));
        Account destinationAccount = accountRepository.findByAccountNumberForUpdate(request.getDestinationAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account", "accountNumber", request.getDestinationAccountNumber()));

        validateAccountActive(sourceAccount);
        validateAccountActive(destinationAccount);

        BigDecimal amount = request.getAmount();

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(sourceAccount.getAccountNumber(), amount, sourceAccount.getBalance());
        }

        if (sourceAccount.getDailyTransferLimit() != null && sourceAccount.getDailyTransferLimit().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dailyTransferred = transactionRepository.sumTransfersSince(
                    sourceAccount.getId(), getStartOfToday());
            BigDecimal totalAfterTransfer = dailyTransferred.add(amount);
            if (totalAfterTransfer.compareTo(sourceAccount.getDailyTransferLimit()) > 0) {
                throw new TransferLimitExceededException(
                        sourceAccount.getAccountNumber(), sourceAccount.getDailyTransferLimit(), totalAfterTransfer);
            }
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        sourceAccount.setDailyTransferredAmount(sourceAccount.getDailyTransferredAmount().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .senderAccount(sourceAccount)
                .receiverAccount(destinationAccount)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Transfer from " + sourceAccount.getAccountNumber() + " to " + destinationAccount.getAccountNumber())
                .build();

        transaction = transactionRepository.save(transaction);

        eventPublisher.publishEvent(new MoneyTransferredEvent(
                sourceAccount.getId(), sourceAccount.getAccountNumber(),
                destinationAccount.getId(), destinationAccount.getAccountNumber(),
                amount, transaction.getReferenceNumber(),
                sourceAccount.getUser().getId(), destinationAccount.getUser().getId()));
        eventPublisher.publishEvent(new TransactionCompletedEvent(
                transaction.getId(), transaction.getReferenceNumber(), amount,
                TransactionType.TRANSFER.name(), sourceAccount.getUser().getId()));

        log.info("Transfer successful: {} from {} to {}", amount, sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber());
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse reverseTransaction(UUID transactionId, String reason) {
        Transaction originalTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "id", transactionId));

        if (originalTransaction.getStatus() == TransactionStatus.REVERSED) {
            throw new BusinessException("Transaction is already reversed");
        }

        if (originalTransaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new BusinessException("Only successful transactions can be reversed");
        }

        Account senderAccount = originalTransaction.getSenderAccount();
        Account receiverAccount = originalTransaction.getReceiverAccount();
        BigDecimal amount = originalTransaction.getAmount();

        switch (originalTransaction.getTransactionType()) {
            case DEPOSIT -> {
                receiverAccount.setBalance(receiverAccount.getBalance().subtract(amount));
                accountRepository.save(receiverAccount);
            }
            case WITHDRAW -> {
                senderAccount.setBalance(senderAccount.getBalance().add(amount));
                accountRepository.save(senderAccount);
            }
            case TRANSFER -> {
                senderAccount.setBalance(senderAccount.getBalance().add(amount));
                receiverAccount.setBalance(receiverAccount.getBalance().subtract(amount));
                senderAccount.setDailyTransferredAmount(
                        senderAccount.getDailyTransferredAmount().subtract(amount));
                accountRepository.save(senderAccount);
                accountRepository.save(receiverAccount);
            }
            default -> throw new BusinessException("Transaction type cannot be reversed");
        }

        originalTransaction.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(originalTransaction);

        Transaction reversalTransaction = Transaction.builder()
                .referenceNumber(generateReferenceNumber())
                .senderAccount(receiverAccount)
                .receiverAccount(senderAccount)
                .amount(amount)
                .transactionType(TransactionType.REVERSAL)
                .status(TransactionStatus.SUCCESS)
                .description(reason != null ? reason : "Reversal of transaction " + originalTransaction.getReferenceNumber())
                .build();

        reversalTransaction = transactionRepository.save(reversalTransaction);
        log.info("Transaction reversed: {} - reversal: {}", originalTransaction.getReferenceNumber(), reversalTransaction.getReferenceNumber());
        return transactionMapper.toResponse(reversalTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "id", transactionId));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReferenceNumber(String referenceNumber) {
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "referenceNumber", referenceNumber));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionPageResponse getTransactionHistory(
            String search,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            UUID accountId,
            Pageable pageable
    ) {
        var specification = TransactionSpecification.withFilters(
                search, transactionType, status, minAmount, maxAmount, fromDate, toDate, accountId);

        Page<Transaction> transactionPage = transactionRepository.findAll(specification, pageable);

        List<TransactionResponse> content = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .toList();

        return TransactionPageResponse.builder()
                .content(content)
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionPageResponse getMyTransactions(
            UUID userId,
            String search,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        List<Account> userAccounts = accountRepository.findByUserId(userId);
        if (userAccounts.isEmpty()) {
            return TransactionPageResponse.builder()
                    .content(List.of())
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
        }

        Specification<Transaction> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> accountPredicates = new ArrayList<>();
            for (Account account : userAccounts) {
                accountPredicates.add(criteriaBuilder.equal(root.get("senderAccount").get("id"), account.getId()));
                accountPredicates.add(criteriaBuilder.equal(root.get("receiverAccount").get("id"), account.getId()));
            }
            Predicate accountPredicate = criteriaBuilder.or(accountPredicates.toArray(new Predicate[0]));

            Predicate filterPredicate = TransactionSpecification.withFilters(
                    search, transactionType, status, minAmount, maxAmount, fromDate, toDate, null)
                    .toPredicate(root, query, criteriaBuilder);

            return criteriaBuilder.and(accountPredicate, filterPredicate);
        };

        Page<Transaction> transactionPage = transactionRepository.findAll(specification, pageable);

        List<TransactionResponse> content = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .toList();

        return TransactionPageResponse.builder()
                .content(content)
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account", "accountNumber", accountNumber));
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active. Current status: " + account.getStatus());
        }
    }

    private String generateReferenceNumber() {
        String referenceNumber;
        do {
            referenceNumber = "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (transactionRepository.existsByReferenceNumber(referenceNumber));
        return referenceNumber;
    }

    private LocalDateTime getStartOfToday() {
        return LocalDate.now().atStartOfDay();
    }
}
