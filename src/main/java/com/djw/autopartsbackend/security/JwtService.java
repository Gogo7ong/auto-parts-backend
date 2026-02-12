package com.djw.autopartsbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        if (!StringUtils.hasText(properties.getSecret()) || properties.getSecret().length() < 32) {
            throw new IllegalStateException("jwt.secret 必须配置且长度至少 32 位（用于 HS256）");
        }
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getExpirationSeconds());

        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = parse(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public Jws<Claims> parse(String token) {
        String raw = normalizeToken(token);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(raw);
    }

    private String normalizeToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("token 为空");
        }
        String raw = token.trim();
        if (raw.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            raw = raw.substring("Bearer ".length()).trim();
        }
        return raw;
    }
}

