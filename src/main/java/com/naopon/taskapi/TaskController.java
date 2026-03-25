package com.naopon.taskapi;

import com.naopon.taskapi.model.Task;
import com.naopon.taskapi.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.naopon.taskapi.dto.TaskRequest;
import com.naopon.taskapi.dto.TaskResponse;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;

import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

@RestController
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping("/tasks")
    //public Task create(@RequestBody Task task) {
    public TaskResponse create(@Valid @RequestBody TaskRequest request){
        Task created = service.create(new Task(null, request.getTitle()));
        //return service.create(task);
        return service.toResponse(created);
    }

    @GetMapping("/tasks")
    public Page<TaskResponse> getAll(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String title
    ) {
        if (title != null && !title.isBlank()) {
            return service.search(title, pageable)
                    .map(service::toResponse);
        }

        return service.findAll(pageable)
                .map(service::toResponse);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getById(@PathVariable long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Task> update(@PathVariable long id, @RequestBody Task updatedTask) {
        return ResponseEntity.ok(service.update(id, updatedTask));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}