package com.mostafa.nexus_bank.auth.repository;

import com.mostafa.nexus_bank.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndVerifiedFalse(String token);

    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.verified = true AND v.expiryDate < :cutoff")
    void deleteVerifiedAndExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}