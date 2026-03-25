package com.naopon.taskapi.dto;

public class TaskResponse {

    private Long id;
    private String title;

    public TaskResponse(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}