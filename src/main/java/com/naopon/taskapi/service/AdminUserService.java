package com.naopon.taskapi.service;

import com.naopon.taskapi.dto.AdminUserCreateRequest;
import com.naopon.taskapi.dto.AdminUserResponse;
import com.naopon.taskapi.dto.AdminUserUpdateRequest;
import com.naopon.taskapi.exception.DuplicateUserException;
import com.naopon.taskapi.exception.NotFoundException;
import com.naopon.taskapi.model.AppUser;
import com.naopon.taskapi.repository.AppUserRepository;
import com.naopon.taskapi.security.RefreshTokenService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Manages application users for admin-facing operations.
@Service
public class AdminUserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AdminUserService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse findById(Long id) {
        return toResponse(loadUser(id));
    }

    @Transactional
    public AdminUserResponse create(AdminUserCreateRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateUserException("username already exists");
        }

        AppUser user = new AppUser(
                null,
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        user.setEnabled(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse update(Long id, AdminUserUpdateRequest request) {
        AppUser user = loadUser(id);
        boolean securityStateChanged = false;

        if (request.getRole() != null && request.getRole() != user.getRole()) {
            user.setRole(request.getRole());
            securityStateChanged = true;
        }

        if (request.getEnabled() != null && request.getEnabled() != user.isEnabled()) {
            user.setEnabled(request.getEnabled());
            securityStateChanged = true;
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            securityStateChanged = true;
        }

        if (securityStateChanged) {
            user.setTokenVersion(user.getTokenVersion() + 1);
            refreshTokenService.revokeAllForUser(user);
        }

        return toResponse(userRepository.save(user));
    }

    private AppUser loadUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    private AdminUserResponse toResponse(AppUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.isEnabled()
        );
    }
}
