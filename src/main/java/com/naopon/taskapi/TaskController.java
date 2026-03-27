package com.naopon.taskapi;

import com.naopon.taskapi.dto.TaskRequest;
import com.naopon.taskapi.dto.TaskResponse;
import com.naopon.taskapi.model.Task;
import com.naopon.taskapi.service.TaskService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Handles HTTP requests related to tasks.
@RestController
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService service;

    // Spring injects TaskService through the constructor.
    public TaskController(TaskService service) {
        this.service = service;
    }

    // Creates a new task from the request body sent by the client.
    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        Task created = service.create(new Task(null, request.getTitle()));
        TaskResponse response = service.toResponse(created);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    // Returns tasks as a paged list. If title is provided, it filters by title.
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

    // Returns one task by its ID.
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable long id) {
        return ResponseEntity.ok(service.toResponse(service.findById(id)));
    }

    // Updates the title of an existing task.
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable long id,
            @Valid @RequestBody TaskRequest request
    ) {
        Task updatedTask = service.update(id, request.getTitle());
        return ResponseEntity.ok(service.toResponse(updatedTask));
    }

    // Deletes the task with the specified ID.
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
