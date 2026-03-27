package com.naopon.taskapi.auth;

import com.naopon.taskapi.model.AppUser;
import com.naopon.taskapi.repository.AppUserRepository;
import com.naopon.taskapi.security.AppUserPrincipal;
import com.naopon.taskapi.security.AuditLogService;
import com.naopon.taskapi.security.JwtService;
import com.naopon.taskapi.security.LoginRateLimitService;
import com.naopon.taskapi.security.RefreshTokenService;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Coordinates authentication, token issuance, refresh, and logout.
@Service
public class AuthService {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginRateLimitService loginRateLimitService;
    private final AuditLogService auditLogService;

    public AuthService(
            AuthenticationConfiguration authenticationConfiguration,
            AppUserRepository userRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            LoginRateLimitService loginRateLimitService,
            AuditLogService auditLogService
    ) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.loginRateLimitService = loginRateLimitService;
        this.auditLogService = auditLogService;
    }

    public LoginResponse login(LoginRequest request, String ipAddress) {
        String rateLimitKey = rateLimitKey(request.getUsername(), ipAddress);
        loginRateLimitService.checkAllowed(rateLimitKey);

        try {
            Authentication authentication = authenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            loginRateLimitService.recordSuccess(rateLimitKey);

            AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
            AppUser user = loadUser(principal.getUsername());

            auditLogService.loginSucceeded(user.getUsername(), ipAddress);
            return issueTokenPair(user);
        } catch (AuthenticationException ex) {
            loginRateLimitService.recordFailure(rateLimitKey);
            auditLogService.loginFailed(request.getUsername(), ipAddress);
            if (loginRateLimitService.isBlocked(rateLimitKey)) {
                auditLogService.loginRateLimited(request.getUsername(), ipAddress);
            }
            throw ex;
        }
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken, String ipAddress) {
        loginRateLimitService.checkAllowed("refresh:" + ipAddress);
        try {
            AppUser user = loadActiveUser(refreshTokenService.consume(rawRefreshToken).getId());
            LoginResponse response = issueTokenPair(user);
            loginRateLimitService.recordSuccess("refresh:" + ipAddress);
            auditLogService.refreshSucceeded(user.getUsername(), ipAddress);
            return response;
        } catch (RuntimeException ex) {
            loginRateLimitService.recordFailure("refresh:" + ipAddress);
            auditLogService.refreshFailed(ipAddress);
            throw ex;
        }
    }

    @Transactional
    public void logout(Authentication authentication, String ipAddress) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        AppUser user = loadActiveUser(principal.getUserId());
        user.setTokenVersion(user.getTokenVersion() + 1);
        refreshTokenService.revokeAllForUser(user);
        auditLogService.logoutSucceeded(user.getUsername(), ipAddress);
    }

    private LoginResponse issueTokenPair(AppUser user) {
        AppUserPrincipal principal = new AppUserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = refreshTokenService.issue(user);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessExpiresInSeconds(),
                jwtService.getRefreshExpiresInSeconds(),
                user.getRole().name()
        );
    }

    private AppUser loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }

    private AppUser loadActiveUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        if (!user.isEnabled()) {
            throw new com.naopon.taskapi.exception.InvalidRefreshTokenException("Invalid or expired refresh token");
        }
        return user;
    }

    private String rateLimitKey(String username, String ipAddress) {
        return username + ":" + ipAddress;
    }

    private org.springframework.security.authentication.AuthenticationManager authenticationManager() {
        try {
            return authenticationConfiguration.getAuthenticationManager();
        } catch (Exception ex) {
            throw new IllegalStateException("Authentication manager is not available", ex);
        }
    }
}
