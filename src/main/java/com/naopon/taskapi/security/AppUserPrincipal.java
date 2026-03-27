package com.naopon.taskapi.security;

import com.naopon.taskapi.model.AppRole;
import com.naopon.taskapi.model.AppUser;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Security principal backed by an AppUser entity.
public class AppUserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final String primaryRole;
    private final Integer tokenVersion;
    private final Set<GrantedAuthority> authorities;

    public AppUserPrincipal(AppUser user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.enabled = user.isEnabled();
        this.primaryRole = user.getRole().name();
        this.tokenVersion = user.getTokenVersion();
        this.authorities = authoritiesFor(user.getRole());
    }

    private Set<GrantedAuthority> authoritiesFor(AppRole role) {
        Set<GrantedAuthority> values = new LinkedHashSet<>();
        values.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        if (role == AppRole.ADMIN) {
            values.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return values;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }

    public Set<String> getAuthorityNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
