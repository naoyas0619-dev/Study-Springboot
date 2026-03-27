package com.naopon.taskapi.auth;

import jakarta.validation.constraints.NotBlank;

// Request payload used to exchange a refresh token for a new token pair.
public class RefreshTokenRequest {

    @NotBlank(message = "must not be blank")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
