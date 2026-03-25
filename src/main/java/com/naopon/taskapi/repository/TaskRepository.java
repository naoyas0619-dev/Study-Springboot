package com.naopon.taskapi.repository;

import com.naopon.taskapi.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository layer that talks to the database for Task entities.
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Spring generates the SQL for this method from its name.
    Page<Task> findByTitleContaining(String title, Pageable pageable);

}
