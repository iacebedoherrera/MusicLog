package com.musiclog.shared.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "musiclog.jwt")
public record JwtProperties(
        String secret,
        Duration expiration
) {
}
