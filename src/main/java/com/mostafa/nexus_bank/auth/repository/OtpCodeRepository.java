package com.mostafa.nexus_bank.auth.repository;

import com.mostafa.nexus_bank.auth.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findByCode(String code);

    Optional<OtpCode> findByCodeAndPurposeAndVerifiedFalse(String code, com.mostafa.nexus_bank.common.enums.OtpPurpose purpose);

    List<OtpCode> findAllByUserId(UUID userId);

    void deleteByUserIdAndPurposeAndVerifiedFalse(UUID userId, com.mostafa.nexus_bank.common.enums.OtpPurpose purpose);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiryTime < :cutoff")
    void deleteByExpiryTimeBefore(@Param("cutoff") LocalDateTime cutoff);
}