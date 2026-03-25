package com.naopon.taskapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// JPA entity mapped to the tasks table in the database.
@Entity
@Table(name = "tasks")
public class Task {

    // Primary key generated automatically by the database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Timestamp set when the task is first created.
    private LocalDateTime createdAt;

    // Timestamp updated whenever the task is edited.
    private LocalDateTime updatedAt;

    // Main text of the task. It must not be null in the database.
    @Column(nullable = false)
    private String title;

    // Required by JPA when it creates objects from database rows.
    public Task() {}

    // Convenient constructor used when creating a task in application code.
    public Task(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
         this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
