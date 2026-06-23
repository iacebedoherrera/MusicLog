package com.musiclog.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "musiclog.musicbrainz")
public record MusicBrainzProperties(
        String baseUrl,
        String coverArtBaseUrl,
        String userAgent
) {
}
