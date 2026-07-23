package com.mostafa.nexus_bank.user.entity;

import com.mostafa.nexus_bank.account.entity.Account;
import com.mostafa.nexus_bank.auth.entity.OtpCode;
import com.mostafa.nexus_bank.auth.entity.RefreshToken;
import com.mostafa.nexus_bank.auth.entity.VerificationToken;
import com.mostafa.nexus_bank.common.entity.BaseEntity;
import com.mostafa.nexus_bank.notification.entity.Notification;
import com.mostafa.nexus_bank.role.entity.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone"),
                @UniqueConstraint(columnNames = "national_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "national_id", nullable = false, length = 14)
    private String nationalId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private int failedAttempts = 0;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /*
     * User -> Roles
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /*
     * User -> Accounts
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Account> accounts = new HashSet<>();

    /*
     * User -> Refresh Tokens
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    /*
     * User -> Verification Tokens
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<VerificationToken> verificationTokens = new HashSet<>();

    /*
     * User -> OTP Codes
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<OtpCode> otpCodes = new HashSet<>();

    /*
     * User -> Notifications
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private Set<Notification> notifications = new HashSet<>();
}
