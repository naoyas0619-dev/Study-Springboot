package com.naopon.taskapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// Persists authentication rate-limit state so it survives restarts and works across instances.
@Entity
@Table(name = "auth_rate_limits")
public class AuthRateLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bucket_key", nullable = false, unique = true, length = 255)
    private String bucketKey;

    @Column(nullable = false)
    private int failureCount;

    private LocalDateTime windowStartedAt;

    private LocalDateTime blockedUntil;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public AuthRateLimit() {}

    public AuthRateLimit(String bucketKey) {
        this.bucketKey = bucketKey;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getBucketKey() {
        return bucketKey;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public LocalDateTime getWindowStartedAt() {
        return windowStartedAt;
    }

    public LocalDateTime getBlockedUntil() {
        return blockedUntil;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setBucketKey(String bucketKey) {
        this.bucketKey = bucketKey;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public void setWindowStartedAt(LocalDateTime windowStartedAt) {
        this.windowStartedAt = windowStartedAt;
    }

    public void setBlockedUntil(LocalDateTime blockedUntil) {
        this.blockedUntil = blockedUntil;
    }
}
