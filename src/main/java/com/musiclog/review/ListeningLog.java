package com.musiclog.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listening_logs")
public class ListeningLog {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "track_mbid", nullable = false, length = 64)
    private String trackMbid;

    @Column(name = "listened_at", nullable = false)
    private Instant listenedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ListeningSource source;

    protected ListeningLog() {
    }

    public ListeningLog(UUID userId, String trackMbid, Instant listenedAt, ListeningSource source) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.trackMbid = trackMbid;
        this.listenedAt = listenedAt;
        this.source = source;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (listenedAt == null) {
            listenedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTrackMbid() {
        return trackMbid;
    }

    public Instant getListenedAt() {
        return listenedAt;
    }

    public ListeningSource getSource() {
        return source;
    }
}
