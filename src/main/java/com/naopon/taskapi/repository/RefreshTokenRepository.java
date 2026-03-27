package com.naopon.taskapi.repository;

import com.naopon.taskapi.model.AppUser;
import com.naopon.taskapi.model.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository for refresh tokens used in token rotation and revocation.
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    long deleteByUser(AppUser user);
}
