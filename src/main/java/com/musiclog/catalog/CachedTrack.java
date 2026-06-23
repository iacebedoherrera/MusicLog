package com.musiclog.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "cached_tracks")
public class CachedTrack {

    @Id
    private String mbid;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "album_mbid")
    private String albumMbid;

    @Column(name = "artist_mbid")
    private String artistMbid;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(name = "cached_at", nullable = false)
    private Instant cachedAt;

    protected CachedTrack() {
    }

    public CachedTrack(String mbid, String title, String albumMbid, String artistMbid, Integer durationMs, Integer trackNumber) {
        this.mbid = mbid;
        this.title = title;
        this.albumMbid = albumMbid;
        this.artistMbid = artistMbid;
        this.durationMs = durationMs;
        this.trackNumber = trackNumber;
        this.cachedAt = Instant.now();
    }

    public String getMbid() {
        return mbid;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbumMbid() {
        return albumMbid;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public Instant getCachedAt() {
        return cachedAt;
    }
}
