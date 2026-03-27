package com.naopon.taskapi.security;

import com.naopon.taskapi.config.AppSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

// Creates and validates signed JWTs used by the API.
@Service
public class JwtService {

    private final AppSecurityProperties properties;
    private final SecretKey signingKey;

    public JwtService(AppSecurityProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(AppUserPrincipal userDetails) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getJwt().getAccessExpiration());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(Map.of(
                        "type", "access",
                        "role", userDetails.getPrimaryRole(),
                        "tokenVersion", userDetails.getTokenVersion(),
                        "authorities", userDetails.getAuthorityNames()
                ))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token, AppUserPrincipal userDetails) {
        Claims claims = parseClaims(token);
        return userDetails.getUsername().equals(claims.getSubject())
                && "access".equals(claims.get("type", String.class))
                && userDetails.getTokenVersion().equals(claims.get("tokenVersion", Integer.class))
                && claims.getExpiration().after(new Date());
    }

    public List<String> extractAuthorities(String token) {
        Claims claims = parseClaims(token);
        Object value = claims.get("authorities");
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long getAccessExpiresInSeconds() {
        return properties.getJwt().getAccessExpiration().toSeconds();
    }

    public long getRefreshExpiresInSeconds() {
        return properties.getJwt().getRefreshExpiration().toSeconds();
    }

    public Claims parseClaims(String token) {
        return parseSignedClaims(token).getPayload();
    }

    private Jws<Claims> parseSignedClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
    }
}
