package com.mostafa.nexus_bank.security.service;

import com.mostafa.nexus_bank.auth.entity.RefreshToken;
import com.mostafa.nexus_bank.auth.repository.RefreshTokenRepository;
import com.mostafa.nexus_bank.exception.InvalidTokenException;
import com.mostafa.nexus_bank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(User user, String token, long expirationMillis) {
        log.debug("Creating refresh token for user: {}", user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .expiryDate(LocalDateTime.now().plusSeconds(expirationMillis / 1000))
                .revoked(false)
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        log.debug("Validating refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found");
                    return new InvalidTokenException("Refresh token not found");
                });

        if (refreshToken.isRevoked()) {
            log.warn("Refresh token has been revoked");
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token has expired");
            throw new InvalidTokenException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        log.debug("Revoking refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found for revocation");
                    return new InvalidTokenException("Refresh token not found");
                });

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token revoked successfully");
    }

    @Transactional
    public void revokeAllUserRefreshTokens(User user) {
        log.debug("Revoking all refresh tokens for user: {}", user.getEmail());

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
        }
        refreshTokenRepository.saveAll(activeTokens);

        log.debug("Revoked {} refresh tokens for user: {}", activeTokens.size(), user.getEmail());
    }

    @Transactional
    public void deleteExpiredTokens() {
        log.debug("Deleting expired refresh tokens");

        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());

        log.debug("Expired refresh tokens deleted");
    }
}
