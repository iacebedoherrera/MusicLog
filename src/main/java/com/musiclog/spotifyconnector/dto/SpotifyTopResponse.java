package com.musiclog.spotifyconnector.dto;

import java.time.Instant;
import java.util.List;

public record SpotifyTopResponse(
        List<SpotifyTopItem> items,
        Instant cachedAt
) {
    public static SpotifyTopResponse empty() {
        return new SpotifyTopResponse(List.of(), null);
    }

    public record SpotifyTopItem(
            String spotifyId,
            String name,
            String subtitle,
            Integer popularity
    ) {
    }
}
