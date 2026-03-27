package com.naopon.taskapi.security;

import com.naopon.taskapi.config.AppSecurityProperties;
import com.naopon.taskapi.exception.TooManyRequestsException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

// Applies a simple in-memory rate limit to auth endpoints to slow brute-force attacks.
@Service
public class LoginRateLimitService {

    private final AppSecurityProperties properties;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public LoginRateLimitService(AppSecurityProperties properties) {
        this.properties = properties;
    }

    public void checkAllowed(String key) {
        Instant now = Instant.now();
        AttemptWindow state = attempts.computeIfAbsent(key, unused -> new AttemptWindow());

        synchronized (state) {
            if (state.blockedUntil != null && now.isBefore(state.blockedUntil)) {
                throw new TooManyRequestsException("Too many authentication attempts. Try again later.");
            }

            if (state.windowStartedAt == null || now.isAfter(state.windowStartedAt.plus(properties.getRateLimit().getWindow()))) {
                state.windowStartedAt = now;
                state.failureCount = 0;
                state.blockedUntil = null;
            }
        }
    }

    public void recordFailure(String key) {
        Instant now = Instant.now();
        AttemptWindow state = attempts.computeIfAbsent(key, unused -> new AttemptWindow());

        synchronized (state) {
            if (state.windowStartedAt == null || now.isAfter(state.windowStartedAt.plus(properties.getRateLimit().getWindow()))) {
                state.windowStartedAt = now;
                state.failureCount = 0;
            }

            state.failureCount++;
            if (state.failureCount >= properties.getRateLimit().getMaxAttempts()) {
                state.blockedUntil = now.plus(properties.getRateLimit().getBlockDuration());
            }
        }
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    public boolean isBlocked(String key) {
        AttemptWindow state = attempts.get(key);
        return state != null
                && state.blockedUntil != null
                && Instant.now().isBefore(state.blockedUntil);
    }

    public void clearAll() {
        attempts.clear();
    }

    private static class AttemptWindow {
        private int failureCount;
        private Instant windowStartedAt;
        private Instant blockedUntil;
    }
}
