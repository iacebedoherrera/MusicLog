CREATE TABLE cached_artists (
    mbid VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sort_name VARCHAR(255),
    country VARCHAR(10),
    disambiguation TEXT,
    cached_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE cached_albums (
    mbid VARCHAR(64) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    artist_mbid VARCHAR(64),
    release_date VARCHAR(30),
    type VARCHAR(50),
    cover_art_url VARCHAR(800),
    cached_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE cached_tracks (
    mbid VARCHAR(64) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    album_mbid VARCHAR(64),
    artist_mbid VARCHAR(64),
    duration_ms INTEGER,
    track_number INTEGER,
    cached_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_cached_artists_name ON cached_artists (LOWER(name));
CREATE INDEX idx_cached_albums_artist_mbid ON cached_albums (artist_mbid);
CREATE INDEX idx_cached_albums_title ON cached_albums (LOWER(title));
CREATE INDEX idx_cached_tracks_album_mbid ON cached_tracks (album_mbid);
CREATE INDEX idx_cached_tracks_artist_mbid ON cached_tracks (artist_mbid);
CREATE INDEX idx_cached_tracks_title ON cached_tracks (LOWER(title));
