package com.musiclog.spotifyconnector;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "spotify_connections")
public class SpotifyConnection {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "spotify_user_id", nullable = false, unique = true, length = 100)
    private String spotifyUserId;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at", nullable = false)
    private Instant tokenExpiresAt;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    protected SpotifyConnection() {
    }

    public SpotifyConnection(UUID userId, String spotifyUserId, String accessToken, String refreshToken, Instant tokenExpiresAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.spotifyUserId = spotifyUserId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.connectedAt = Instant.now();
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (connectedAt == null) {
            connectedAt = Instant.now();
        }
    }

    public void updateTokens(String accessToken, String refreshToken, Instant tokenExpiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public void markSynced(Instant syncedAt) {
        this.lastSyncAt = syncedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSpotifyUserId() {
        return spotifyUserId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public Instant getLastSyncAt() {
        return lastSyncAt;
    }
}
