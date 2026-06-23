CREATE TABLE spotify_connections (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    spotify_user_id VARCHAR(100) NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    token_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_sync_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_spotify_connections_user UNIQUE (user_id),
    CONSTRAINT uk_spotify_connections_spotify_user UNIQUE (spotify_user_id)
);

CREATE INDEX idx_spotify_connections_token_expires_at ON spotify_connections (token_expires_at);
CREATE INDEX idx_spotify_connections_last_sync_at ON spotify_connections (last_sync_at);
