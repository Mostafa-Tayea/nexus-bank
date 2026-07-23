package com.mostafa.nexus_bank.auth.repository;

import com.mostafa.nexus_bank.auth.entity.RefreshToken;
import com.mostafa.nexus_bank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    List<RefreshToken> findByUserAndRevokedFalse(User user);

    void deleteByExpiryDateBefore(java.time.LocalDateTime date);

}