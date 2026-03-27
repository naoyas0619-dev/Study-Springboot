package com.naopon.taskapi.auth;

// Token payload returned after a successful login.
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long accessExpiresInSeconds;
    private final long refreshExpiresInSeconds;
    private final String role;

    public LoginResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long accessExpiresInSeconds,
            long refreshExpiresInSeconds,
            String role
    ) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.accessExpiresInSeconds = accessExpiresInSeconds;
        this.refreshExpiresInSeconds = refreshExpiresInSeconds;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getAccessExpiresInSeconds() {
        return accessExpiresInSeconds;
    }

    public long getRefreshExpiresInSeconds() {
        return refreshExpiresInSeconds;
    }

    public String getRole() {
        return role;
    }
}
