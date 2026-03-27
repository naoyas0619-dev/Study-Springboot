package com.naopon.taskapi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// Writes security-relevant events to the application log.
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    public void loginSucceeded(String username, String ipAddress) {
        log.info("audit_event=login_succeeded username={} ip={}", username, ipAddress);
    }

    public void loginFailed(String username, String ipAddress) {
        log.warn("audit_event=login_failed username={} ip={}", username, ipAddress);
    }

    public void loginRateLimited(String username, String ipAddress) {
        log.warn("audit_event=login_rate_limited username={} ip={}", username, ipAddress);
    }

    public void refreshSucceeded(String username, String ipAddress) {
        log.info("audit_event=refresh_succeeded username={} ip={}", username, ipAddress);
    }

    public void refreshFailed(String ipAddress) {
        log.warn("audit_event=refresh_failed ip={}", ipAddress);
    }

    public void logoutSucceeded(String username, String ipAddress) {
        log.info("audit_event=logout_succeeded username={} ip={}", username, ipAddress);
    }

    public void invalidAccessToken(String path, String ipAddress) {
        log.warn("audit_event=invalid_access_token path={} ip={}", path, ipAddress);
    }
}
