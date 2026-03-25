package com.naopon.taskapi.dto;

// Output object returned to the client.
public class TaskResponse {

    private Long id;
    private String title;

    // The response contains only the fields we want to expose in the API.
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
