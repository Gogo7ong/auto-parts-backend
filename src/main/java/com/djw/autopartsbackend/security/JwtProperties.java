package com.djw.autopartsbackend.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private String issuer = "auto-parts-backend";
    private long expirationSeconds = 60 * 60 * 8;
}

