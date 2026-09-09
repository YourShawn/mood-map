package com.moodmap.common.security;

import com.moodmap.common.config.AppProperties;
import com.moodmap.common.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final AppProperties appProperties;
    private final SecretKey key;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
        byte[] bytes = appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String issue(String userId, String nickname) {
        Instant now = Instant.now();
        Instant exp = now.plus(appProperties.getJwt().getTtlHours(), ChronoUnit.HOURS);
        var builder = Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp));
        if (nickname != null && !nickname.isBlank()) {
            builder.claim("nick", nickname);
        }
        return builder.signWith(key).compact();
    }

    public UserPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String nick = claims.get("nick", String.class);
            return new UserPrincipal(claims.getSubject(), nick);
        } catch (Exception ex) {
            throw ApiException.unauthorized("Invalid or expired session");
        }
    }
}
