package com.naopon.taskapi.dto;

// API response for application user management.
public class AdminUserResponse {

    private final Long id;
    private final String username;
    private final String role;
    private final boolean enabled;

    public AdminUserResponse(Long id, String username, String role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
