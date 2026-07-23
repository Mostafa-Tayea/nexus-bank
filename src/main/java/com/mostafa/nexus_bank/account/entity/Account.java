package com.mostafa.nexus_bank.account.entity;

import com.mostafa.nexus_bank.common.entity.BaseEntity;
import com.mostafa.nexus_bank.common.enums.AccountStatus;
import com.mostafa.nexus_bank.common.enums.AccountType;
import com.mostafa.nexus_bank.transaction.entity.Transaction;
import com.mostafa.nexus_bank.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, unique = true, length = 34)
    private String iban;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "EGP";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "daily_transfer_limit", precision = 19, scale = 4)
    private BigDecimal dailyTransferLimit;

    @Column(name = "daily_transferred_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal dailyTransferredAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "senderAccount")
    @Builder.Default
    private Set<Transaction> sentTransactions = new HashSet<>();

    @OneToMany(mappedBy = "receiverAccount")
    @Builder.Default
    private Set<Transaction> receivedTransactions = new HashSet<>();
}
