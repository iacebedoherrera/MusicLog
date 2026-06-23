package com.musiclog.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "cached_artists")
public class CachedArtist {

    @Id
    private String mbid;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_name")
    private String sortName;

    private String country;

    @Column(columnDefinition = "TEXT")
    private String disambiguation;

    @Column(name = "cached_at", nullable = false)
    private Instant cachedAt;

    protected CachedArtist() {
    }

    public CachedArtist(String mbid, String name, String sortName, String country, String disambiguation) {
        this.mbid = mbid;
        this.name = name;
        this.sortName = sortName;
        this.country = country;
        this.disambiguation = disambiguation;
        this.cachedAt = Instant.now();
    }

    public String getMbid() {
        return mbid;
    }

    public String getName() {
        return name;
    }

    public String getSortName() {
        return sortName;
    }

    public String getCountry() {
        return country;
    }

    public String getDisambiguation() {
        return disambiguation;
    }

    public Instant getCachedAt() {
        return cachedAt;
    }
}
