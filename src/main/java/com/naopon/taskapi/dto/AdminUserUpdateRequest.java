package com.naopon.taskapi.dto;

import com.naopon.taskapi.model.AppRole;
import jakarta.validation.constraints.Size;

// Request payload for updating role, enabled flag, or password for an application user.
public class AdminUserUpdateRequest {

    private AppRole role;

    private Boolean enabled;

    @Size(min = 12, message = "must be at least 12 characters")
    private String password;

    public AppRole getRole() {
        return role;
    }

    public void setRole(AppRole role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
