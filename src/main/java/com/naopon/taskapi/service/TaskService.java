package com.naopon.taskapi.service;

import com.naopon.taskapi.model.Task;
import com.naopon.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.naopon.taskapi.dto.TaskResponse;

import com.naopon.taskapi.exception.NotFoundException;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
public class TaskService {

    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    public Task create(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        //return repo.save(new Task(null, task.getTitle()));
        return repo.save(task);
    }

    public Page<Task> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Task findById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("task not found"));
    }

    public Task update(long id, Task updatedTask) {
        Task task = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("task not found"));

        task.setTitle(updatedTask.getTitle());
        task.setUpdatedAt(LocalDateTime.now());

        return repo.save(task);
    }

    public void delete(long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("task not found");
        }
        repo.deleteById(id);
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle());
    }

    public List<TaskResponse> toResponseList(List<Task> tasks) {
        return tasks.stream()
            .map(this::toResponse)
            .toList();
    }

    public Page<Task> search(String title, Pageable pageable) {
        return repo.findByTitleContaining(title, pageable);
    }
}