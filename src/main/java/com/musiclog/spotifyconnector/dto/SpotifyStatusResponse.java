package com.musiclog.spotifyconnector.dto;

import java.time.Instant;

public record SpotifyStatusResponse(
        boolean connected,
        String spotifyUserId,
        Instant connectedAt,
        Instant lastSyncAt
) {
    public static SpotifyStatusResponse disconnected() {
        return new SpotifyStatusResponse(false, null, null, null);
    }
}
