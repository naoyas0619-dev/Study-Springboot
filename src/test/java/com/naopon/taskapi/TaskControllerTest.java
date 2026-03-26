package com.naopon.taskapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naopon.taskapi.dto.TaskRequest;
import com.naopon.taskapi.model.Task;
import com.naopon.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createReturnsCreatedTask() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("write tests");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("write tests"));
    }

    @Test
    void createWithBlankTitleReturnsBadRequest() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle(" ");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("title must not be blank"))
                .andExpect(jsonPath("$.path").value("/tasks"));
    }

    @Test
    void getByIdReturnsTask() throws Exception {
        Task task = saveTask("read docs");

        mockMvc.perform(get("/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("read docs"));
    }

    @Test
    void getByIdWithMissingIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/9999"));
    }

    @Test
    void getAllReturnsPagedTasks() throws Exception {
        saveTask("task 1");
        saveTask("task 2");
        saveTask("task 3");

        mockMvc.perform(get("/tasks")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void searchReturnsMatchingTasks() throws Exception {
        saveTask("alpha");
        saveTask("alphabet");
        saveTask("beta");

        mockMvc.perform(get("/tasks")
                        .param("title", "alpha")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("alpha"))
                .andExpect(jsonPath("$.content[1].title").value("alphabet"));
    }

    @Test
    void updateReturnsUpdatedTask() throws Exception {
        Task task = saveTask("before");
        TaskRequest request = new TaskRequest();
        request.setTitle("after");

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("after"));
    }

    @Test
    void updateWithBlankTitleReturnsBadRequest() throws Exception {
        Task task = saveTask("before");
        TaskRequest request = new TaskRequest();
        request.setTitle("");

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("title must not be blank"))
                .andExpect(jsonPath("$.path").value("/tasks/" + task.getId()));
    }

    @Test
    void updateWithMissingIdReturnsNotFound() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("after");

        mockMvc.perform(put("/tasks/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/9999"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        Task task = saveTask("cleanup");

        mockMvc.perform(delete("/tasks/{id}", task.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", task.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWithMissingIdReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/9999"));
    }

    private Task saveTask(String title) {
        return repository.save(new Task(null, title));
    }
}
