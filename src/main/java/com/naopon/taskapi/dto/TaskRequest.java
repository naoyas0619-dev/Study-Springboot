package com.naopon.taskapi.dto;

import jakarta.validation.constraints.NotBlank;

// Input object used when the client sends data to create a task.
public class TaskRequest {

    // Reject empty titles before the request reaches the service layer.
    @NotBlank
    private String title;

    // Default constructor is required so Spring can create this object from JSON.
    public TaskRequest() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
