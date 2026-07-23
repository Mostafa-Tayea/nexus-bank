package com.mostafa.nexus_bank.transaction.repository;

import com.mostafa.nexus_bank.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    boolean existsByReferenceNumber(String referenceNumber);

    @Query("SELECT t FROM Transaction t WHERE t.senderAccount.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findBySenderIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t WHERE t.receiverAccount.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByReceiverIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t WHERE t.senderAccount.id = :accountId OR t.receiverAccount.id = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.receiverAccount.id = :accountId AND t.status = 'SUCCESS' AND t.createdAt >= :since")
    java.math.BigDecimal sumDepositsSince(@Param("accountId") UUID accountId, @Param("since") java.time.LocalDateTime since);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.senderAccount.id = :accountId AND t.status = 'SUCCESS' AND t.transactionType = 'TRANSFER' AND t.createdAt >= :since")
    java.math.BigDecimal sumTransfersSince(@Param("accountId") UUID accountId, @Param("since") java.time.LocalDateTime since);
}