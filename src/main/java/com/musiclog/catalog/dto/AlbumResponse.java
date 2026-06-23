package com.musiclog.catalog.dto;

import com.musiclog.catalog.CachedAlbum;

public record AlbumResponse(
        String mbid,
        String title,
        String artistMbid,
        String releaseDate,
        String type,
        String coverArtUrl
) {
    public static AlbumResponse from(CachedAlbum album) {
        return new AlbumResponse(
                album.getMbid(),
                album.getTitle(),
                album.getArtistMbid(),
                album.getReleaseDate(),
                album.getType(),
                album.getCoverArtUrl());
    }
}
