package com.naopon.taskapi.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskRequest {

    @NotBlank
    private String title;

    public TaskRequest() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}