package com.naopon.taskapi.repository;

import com.naopon.taskapi.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository for persistent API users.
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}
