package com.musiclog.catalog.dto;

import com.musiclog.catalog.CachedArtist;

public record ArtistResponse(
        String mbid,
        String name,
        String sortName,
        String country,
        String disambiguation
) {
    public static ArtistResponse from(CachedArtist artist) {
        return new ArtistResponse(artist.getMbid(), artist.getName(), artist.getSortName(), artist.getCountry(), artist.getDisambiguation());
    }
}
