package com.naopon.taskapi.service;

import com.naopon.taskapi.exception.NotFoundException;
import com.naopon.taskapi.model.Task;
import com.naopon.taskapi.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    @Test
    void createSetsTimestampsBeforeSaving() {
        Task input = new Task(null, "write service tests");
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task saved = service.create(input);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(captor.capture());
        Task persisted = captor.getValue();

        assertThat(saved).isSameAs(persisted);
        assertThat(ReflectionTestUtils.getField(persisted, "createdAt")).isInstanceOf(LocalDateTime.class);
        assertThat(ReflectionTestUtils.getField(persisted, "updatedAt")).isInstanceOf(LocalDateTime.class);
        assertThat(persisted.getTitle()).isEqualTo("write service tests");
    }

    @Test
    void updateChangesTitleAndRefreshesUpdatedAt() {
        Task existing = new Task(1L, "before");
        LocalDateTime previousUpdatedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        ReflectionTestUtils.setField(existing, "updatedAt", previousUpdatedAt);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = service.update(1L, "after");

        assertThat(updated.getTitle()).isEqualTo("after");
        LocalDateTime actualUpdatedAt = (LocalDateTime) ReflectionTestUtils.getField(updated, "updatedAt");
        assertThat(actualUpdatedAt).isAfter(previousUpdatedAt);
    }

    @Test
    void findByIdWithMissingTaskThrowsNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("task not found");
    }

    @Test
    void deleteWithMissingTaskThrowsNotFoundException() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("task not found");
    }
}
