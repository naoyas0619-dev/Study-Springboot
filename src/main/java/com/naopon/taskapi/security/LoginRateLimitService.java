package com.naopon.taskapi.security;

import com.naopon.taskapi.config.AppSecurityProperties;
import com.naopon.taskapi.exception.TooManyRequestsException;
import com.naopon.taskapi.model.AuthRateLimit;
import com.naopon.taskapi.repository.AuthRateLimitRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Persists authentication rate-limit state in the database.
@Service
public class LoginRateLimitService {

    private final AppSecurityProperties properties;
    private final AuthRateLimitRepository authRateLimitRepository;

    public LoginRateLimitService(
            AppSecurityProperties properties,
            AuthRateLimitRepository authRateLimitRepository
    ) {
        this.properties = properties;
        this.authRateLimitRepository = authRateLimitRepository;
    }

    @Transactional
    public void checkAllowed(String key) {
        LocalDateTime now = LocalDateTime.now();
        AuthRateLimit state = loadState(key);

        if (state.getBlockedUntil() != null && now.isBefore(state.getBlockedUntil())) {
            throw new TooManyRequestsException("Too many authentication attempts. Try again later.");
        }

        if (state.getWindowStartedAt() == null
                || now.isAfter(state.getWindowStartedAt().plus(properties.getRateLimit().getWindow()))) {
            state.setWindowStartedAt(now);
            state.setFailureCount(0);
            state.setBlockedUntil(null);
        }

        authRateLimitRepository.save(state);
    }

    @Transactional
    public void recordFailure(String key) {
        LocalDateTime now = LocalDateTime.now();
        AuthRateLimit state = loadState(key);

        if (state.getWindowStartedAt() == null
                || now.isAfter(state.getWindowStartedAt().plus(properties.getRateLimit().getWindow()))) {
            state.setWindowStartedAt(now);
            state.setFailureCount(0);
            state.setBlockedUntil(null);
        }

        state.setFailureCount(state.getFailureCount() + 1);
        if (state.getFailureCount() >= properties.getRateLimit().getMaxAttempts()) {
            state.setBlockedUntil(now.plus(properties.getRateLimit().getBlockDuration()));
        }

        authRateLimitRepository.save(state);
    }

    @Transactional
    public void recordSuccess(String key) {
        authRateLimitRepository.findByBucketKey(key)
                .ifPresent(authRateLimitRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(String key) {
        return authRateLimitRepository.findByBucketKey(key)
                .map(state -> state.getBlockedUntil() != null
                        && LocalDateTime.now().isBefore(state.getBlockedUntil()))
                .orElse(false);
    }

    @Transactional
    public void clearAll() {
        authRateLimitRepository.deleteAll();
    }

    private AuthRateLimit loadState(String key) {
        return authRateLimitRepository.findByBucketKey(key)
                .orElseGet(() -> authRateLimitRepository.save(new AuthRateLimit(key)));
    }
}
