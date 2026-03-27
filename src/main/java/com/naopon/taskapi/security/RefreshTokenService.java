package com.naopon.taskapi.security;

import com.naopon.taskapi.config.AppSecurityProperties;
import com.naopon.taskapi.exception.InvalidRefreshTokenException;
import com.naopon.taskapi.model.AppUser;
import com.naopon.taskapi.model.RefreshToken;
import com.naopon.taskapi.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Issues, rotates, and revokes refresh tokens stored in the database.
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppSecurityProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            AppSecurityProperties properties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.properties = properties;
    }

    @Transactional
    public String issue(AppUser user) {
        String rawToken = generateToken();
        RefreshToken refreshToken = new RefreshToken(
                user,
                hash(rawToken),
                LocalDateTime.now().plus(properties.getJwt().getRefreshExpiration())
        );
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public AppUser consume(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid or expired refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }

        refreshToken.setRevokedAt(LocalDateTime.now());
        return refreshToken.getUser();
    }

    @Transactional
    public void revokeAllForUser(AppUser user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String generateToken() {
        byte[] buffer = new byte[48];
        secureRandom.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hashed) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
