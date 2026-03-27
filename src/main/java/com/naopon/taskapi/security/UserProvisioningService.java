package com.naopon.taskapi.security;

import com.naopon.taskapi.config.AppSecurityProperties;
import com.naopon.taskapi.model.AppRole;
import com.naopon.taskapi.model.AppUser;
import com.naopon.taskapi.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Ensures a bootstrap user exists in the database for local and first-run usage.
@Service
public class UserProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(UserProvisioningService.class);

    private final AppUserRepository userRepository;
    private final AppSecurityProperties properties;
    private final PasswordEncoder passwordEncoder;

    public UserProvisioningService(
            AppUserRepository userRepository,
            AppSecurityProperties properties,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void ensureBootstrapUser() {
        String username = properties.getBootstrap().getUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        AppUser user = new AppUser(
                null,
                username,
                passwordEncoder.encode(properties.getBootstrap().getPassword()),
                AppRole.valueOf(properties.getBootstrap().getRole().toUpperCase())
        );
        userRepository.save(user);
        log.info("Bootstrap user created for username={}", username);
    }

    @org.springframework.context.annotation.Bean
    public ApplicationRunner bootstrapUserRunner() {
        return args -> ensureBootstrapUser();
    }
}
