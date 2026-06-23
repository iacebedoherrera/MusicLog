package com.musiclog.catalog.dto;

import com.musiclog.catalog.CachedTrack;

public record TrackResponse(
        String mbid,
        String title,
        String albumMbid,
        String artistMbid,
        Integer durationMs,
        Integer trackNumber
) {
    public static TrackResponse from(CachedTrack track) {
        return new TrackResponse(
                track.getMbid(),
                track.getTitle(),
                track.getAlbumMbid(),
                track.getArtistMbid(),
                track.getDurationMs(),
                track.getTrackNumber());
    }
}
