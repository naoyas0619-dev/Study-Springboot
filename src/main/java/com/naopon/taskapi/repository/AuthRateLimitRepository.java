package com.naopon.taskapi.repository;

import com.naopon.taskapi.model.AuthRateLimit;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

// Repository for persistent rate-limit buckets.
public interface AuthRateLimitRepository extends JpaRepository<AuthRateLimit, Long> {

    @Lock(PESSIMISTIC_WRITE)
    Optional<AuthRateLimit> findByBucketKey(String bucketKey);
}
