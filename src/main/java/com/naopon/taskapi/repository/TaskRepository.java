package com.naopon.taskapi.repository;

import com.naopon.taskapi.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByTitleContaining(String title, Pageable pageable);

}