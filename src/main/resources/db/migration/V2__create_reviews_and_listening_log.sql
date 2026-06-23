CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    target_mbid VARCHAR(64) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    rating INTEGER,
    review_text TEXT,
    contains_spoilers BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_reviews_target_type CHECK (target_type IN ('ARTIST', 'ALBUM', 'TRACK')),
    CONSTRAINT ck_reviews_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 10),
    CONSTRAINT ck_reviews_content CHECK (rating IS NOT NULL OR review_text IS NOT NULL),
    CONSTRAINT uk_reviews_user_target UNIQUE (user_id, target_mbid)
);

CREATE TABLE listening_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    track_mbid VARCHAR(64) NOT NULL,
    listened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source VARCHAR(30) NOT NULL,
    CONSTRAINT ck_listening_logs_source CHECK (source IN ('MANUAL', 'SPOTIFY_IMPORT'))
);

CREATE INDEX idx_reviews_user_id_created_at ON reviews (user_id, created_at DESC);
CREATE INDEX idx_reviews_target ON reviews (target_mbid, target_type, created_at DESC);
CREATE INDEX idx_listening_logs_user_id_listened_at ON listening_logs (user_id, listened_at DESC);
CREATE INDEX idx_listening_logs_track_mbid ON listening_logs (track_mbid);
