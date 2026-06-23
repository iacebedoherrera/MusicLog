package com.musiclog.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "cached_albums")
public class CachedAlbum {

    @Id
    private String mbid;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "artist_mbid")
    private String artistMbid;

    @Column(name = "release_date")
    private String releaseDate;

    private String type;

    @Column(name = "cover_art_url", length = 800)
    private String coverArtUrl;

    @Column(name = "cached_at", nullable = false)
    private Instant cachedAt;

    protected CachedAlbum() {
    }

    public CachedAlbum(String mbid, String title, String artistMbid, String releaseDate, String type, String coverArtUrl) {
        this.mbid = mbid;
        this.title = title;
        this.artistMbid = artistMbid;
        this.releaseDate = releaseDate;
        this.type = type;
        this.coverArtUrl = coverArtUrl;
        this.cachedAt = Instant.now();
    }

    public String getMbid() {
        return mbid;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getType() {
        return type;
    }

    public String getCoverArtUrl() {
        return coverArtUrl;
    }

    public Instant getCachedAt() {
        return cachedAt;
    }
}
