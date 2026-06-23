package com.musiclog.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "musiclog.spotify")
public record SpotifyProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String tokenEncryptionKey,
        long syncIntervalMs,
        long syncInitialDelayMs,
        String frontendRedirectUri
) {
}
