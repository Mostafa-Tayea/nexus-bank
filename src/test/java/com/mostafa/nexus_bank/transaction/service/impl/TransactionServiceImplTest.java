package com.mostafa.nexus_bank.transaction.service.impl;

import com.mostafa.nexus_bank.account.entity.Account;
import com.mostafa.nexus_bank.account.repository.AccountRepository;
import com.mostafa.nexus_bank.common.enums.AccountStatus;
import com.mostafa.nexus_bank.common.enums.AccountType;
import com.mostafa.nexus_bank.common.enums.TransactionStatus;
import com.mostafa.nexus_bank.common.enums.TransactionType;
import com.mostafa.nexus_bank.exception.BusinessException;
import com.mostafa.nexus_bank.exception.EntityNotFoundException;
import com.mostafa.nexus_bank.exception.InsufficientBalanceException;
import com.mostafa.nexus_bank.exception.TransferLimitExceededException;
import com.mostafa.nexus_bank.transaction.dto.request.DepositRequest;
import com.mostafa.nexus_bank.transaction.dto.request.TransferRequest;
import com.mostafa.nexus_bank.transaction.dto.request.WithdrawRequest;
import com.mostafa.nexus_bank.transaction.dto.response.TransactionResponse;
import com.mostafa.nexus_bank.transaction.entity.Transaction;
import com.mostafa.nexus_bank.transaction.mapper.TransactionMapper;
import com.mostafa.nexus_bank.transaction.repository.TransactionRepository;
import com.mostafa.nexus_bank.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Account sourceAccount;
    private Account destinationAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("01234567890")
                .nationalId("12345678901234")
                .password("encodedPassword")
                .enabled(true)
                .accountNonLocked(true)
                .roles(new HashSet<>())
                .build();
        testUser.setId(UUID.randomUUID());

        sourceAccount = Account.builder()
                .accountNumber("1234567890")
                .iban("EG123456789012345678901234")
                .balance(new BigDecimal("10000.00"))
                .currency("EGP")
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .dailyTransferLimit(new BigDecimal("50000.00"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .user(testUser)
                .build();
        sourceAccount.setId(UUID.randomUUID());

        destinationAccount = Account.builder()
                .accountNumber("0987654321")
                .iban("EG987654321012345678901234")
                .balance(new BigDecimal("5000.00"))
                .currency("EGP")
                .type(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .dailyTransferLimit(new BigDecimal("50000.00"))
                .dailyTransferredAmount(BigDecimal.ZERO)
                .user(testUser)
                .build();
        destinationAccount.setId(UUID.randomUUID());

        testTransaction = Transaction.builder()
                .referenceNumber("TXN1234567890")
                .senderAccount(sourceAccount)
                .receiverAccount(destinationAccount)
                .amount(new BigDecimal("1000.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description("Test transfer")
                .build();
        testTransaction.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Deposit - Success")
    void deposit_Success() {
        DepositRequest request = new DepositRequest("1234567890", new BigDecimal("500.00"), "Test deposit");

        when(accountRepository.findByAccountNumberForUpdate("1234567890"))
                .thenReturn(Optional.of(sourceAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(any())).thenReturn(TransactionResponse.builder().build());

        TransactionResponse response = transactionService.deposit(request);

        assertThat(response).isNotNull();
        assertThat(sourceAccount.getBalance()).isEqualByComparingTo(new BigDecimal("10500.00"));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(eventPublisher, times(2)).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Deposit - Account Not Found")
    void deposit_AccountNotFound() {
        DepositRequest request = new DepositRequest("9999999999", new BigDecimal("500.00"), "Test");

        when(accountRepository.findByAccountNumberForUpdate("9999999999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Deposit - Frozen Account")
    void deposit_FrozenAccount() {
        sourceAccount.setStatus(AccountStatus.FROZEN);
        DepositRequest request = new DepositRequest("1234567890", new BigDecimal("500.00"), "Test");

        when(accountRepository.findByAccountNumberForUpdate("1234567890"))
                .thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("Withdraw - Success")
    void withdraw_Success() {
        WithdrawRequest request = new WithdrawRequest("1234567890", new BigDecimal("1000.00"), "Test withdrawal");

        when(accountRepository.findByAccountNumberForUpdate("1234567890"))
                .thenReturn(Optional.of(sourceAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(any())).thenReturn(TransactionResponse.builder().build());

        TransactionResponse response = transactionService.withdraw(request);

        assertThat(response).isNotNull();
        assertThat(sourceAccount.getBalance()).isEqualByComparingTo(new BigDecimal("9000.00"));
    }

    @Test
    @DisplayName("Withdraw - Insufficient Balance")
    void withdraw_InsufficientBalance() {
        WithdrawRequest request = new WithdrawRequest("1234567890", new BigDecimal("50000.00"), "Test");

        when(accountRepository.findByAccountNumberForUpdate("1234567890"))
                .thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> transactionService.withdraw(request))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("Transfer - Success")
    void transfer_Success() {
        TransferRequest request = new TransferRequest(
                "1234567890", "0987654321", new BigDecimal("1000.00"), "Test transfer");

        when(accountRepository.findByAccountNumberForUpdate("1234567890"))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("0987654321"))
                .thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.sumTransfersSince(eq(sourceAccount.getId()), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(any())).thenReturn(TransactionResponse.builder().build());

        TransactionResponse response = transactionService.transfer(request);

        assertThat(response).isNotNull();
        assertThat(sourceAccount.getBalance()).isEqualByComparingTo(new BigDecimal("9000.00"));
        assertThat(destinationAccount.getBalance()).isEqualByComparingTo(new BigDecimal("6000.00"));
    }

    @Test
    @DisplayName("Transfer - Same Account")
    void transfer_SameAccount() {
        TransferRequest request = new TransferRequest(
                "1234567890", "1234567890", new BigDecimal("1000.00"), "Test");

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("same account");
    }

    @Test
    @DisplayName("Transfer - Insufficient Balance")
    void transfer_InsufficientBalance() {
        TransferRequest request = new TransferRequest(
                "1234567890", "0987654321", new BigDecimal("50000.00"), "Test");

        when(accountRepository.findByAccountNumberForUpdate("1234567890"))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("0987654321"))
                .thenReturn(Optional.of(destinationAccount));

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("Transfer - Exceeds Daily Limit")
    void transfer_ExceedsDailyLimit() {
        sourceAccount.setDailyTransferLimit(new BigDecimal("1000.00"));

        TransferRequest request = new TransferRequest(
                "1234567890", "0987654321", new BigDecimal("2000.00"), "Test");

        when(accountRepository.findByAccountNumberForUpdate("1234567890"))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate("0987654321"))
                .thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.sumTransfersSince(eq(sourceAccount.getId()), any()))
                .thenReturn(new BigDecimal("500.00"));

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(TransferLimitExceededException.class);
    }

    @Test
    @DisplayName("Get Transaction By ID - Success")
    void getTransactionById_Success() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionMapper.toResponse(testTransaction)).thenReturn(TransactionResponse.builder().build());

        TransactionResponse response = transactionService.getTransactionById(transactionId);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Get Transaction By ID - Not Found")
    void getTransactionById_NotFound() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Get Transaction By Reference Number - Success")
    void getTransactionByReferenceNumber_Success() {
        when(transactionRepository.findByReferenceNumber("TXN1234567890"))
                .thenReturn(Optional.of(testTransaction));
        when(transactionMapper.toResponse(testTransaction)).thenReturn(TransactionResponse.builder().build());

        TransactionResponse response = transactionService.getTransactionByReferenceNumber("TXN1234567890");

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Get Transaction By Reference Number - Not Found")
    void getTransactionByReferenceNumber_NotFound() {
        when(transactionRepository.findByReferenceNumber("NONEXISTENT"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionByReferenceNumber("NONEXISTENT"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
