package com.naopon.taskapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point of the Spring Boot application.
@SpringBootApplication
public class TaskApiApplication {

	public static void main(String[] args) {
		// Bootstraps Spring and starts the embedded web server.
		SpringApplication.run(TaskApiApplication.class, args);
	}

}
