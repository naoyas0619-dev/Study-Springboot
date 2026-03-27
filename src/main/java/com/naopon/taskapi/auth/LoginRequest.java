package com.naopon.taskapi.auth;

import jakarta.validation.constraints.NotBlank;

// Login payload sent to the authentication endpoint.
public class LoginRequest {

    @NotBlank(message = "must not be blank")
    private String username;

    @NotBlank(message = "must not be blank")
    private String password;

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
}
