package com.naopon.taskapi.service;

import com.naopon.taskapi.dto.TaskResponse;
import com.naopon.taskapi.exception.NotFoundException;
import com.naopon.taskapi.model.Task;
import com.naopon.taskapi.repository.TaskRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

// Contains business logic for creating, searching, updating, and deleting tasks.
@Service
public class TaskService {

    private final TaskRepository repo;

    // Spring injects the repository used to access the database.
    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    // Adds timestamps and stores a new task.
    public Task create(Task task) {
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return repo.save(task);
    }

    // Returns all tasks using Spring Data pagination.
    public Page<Task> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    // Finds a task by ID or throws an exception if it does not exist.
    public Task findById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("task not found"));
    }

    // Updates only the fields that are allowed to change.
    public Task update(long id, String title) {
        Task task = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("task not found"));

        task.setTitle(title);
        task.setUpdatedAt(LocalDateTime.now());

        return repo.save(task);
    }

    // Deletes a task after confirming it exists.
    public void delete(long id) {
        Task task = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("task not found"));

        repo.delete(task);
    }

    // Converts the entity used inside the app into a simpler API response object.
    public TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle());
    }

    // Searches tasks whose title contains the given text.
    public Page<Task> search(String title, Pageable pageable) {
        return repo.findByTitleContaining(title, pageable);
    }
}
