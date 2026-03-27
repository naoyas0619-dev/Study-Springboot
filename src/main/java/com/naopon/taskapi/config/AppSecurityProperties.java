package com.naopon.taskapi.config;

import java.time.Duration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Externalized settings for bootstrap user, JWT behavior, and auth hardening.
@Validated
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    @Valid
    private final Bootstrap bootstrap = new Bootstrap();

    @Valid
    private final Jwt jwt = new Jwt();

    @Valid
    private final RateLimit rateLimit = new RateLimit();

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public static class Bootstrap {

        @NotBlank
        private String username;

        @NotBlank
        @Size(min = 12)
        private String password;

        @NotBlank
        private String role = "ADMIN";

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class Jwt {

        @NotBlank
        @Size(min = 32)
        private String secret;

        @NotNull
        private Duration accessExpiration = Duration.ofMinutes(15);

        @NotNull
        private Duration refreshExpiration = Duration.ofDays(7);

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getAccessExpiration() {
            return accessExpiration;
        }

        public void setAccessExpiration(Duration accessExpiration) {
            this.accessExpiration = accessExpiration;
        }

        public Duration getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Duration refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }

    public static class RateLimit {

        @Min(1)
        private int maxAttempts = 5;

        @NotNull
        private Duration window = Duration.ofMinutes(15);

        @NotNull
        private Duration blockDuration = Duration.ofMinutes(15);

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }

        public Duration getBlockDuration() {
            return blockDuration;
        }

        public void setBlockDuration(Duration blockDuration) {
            this.blockDuration = blockDuration;
        }
    }
}
