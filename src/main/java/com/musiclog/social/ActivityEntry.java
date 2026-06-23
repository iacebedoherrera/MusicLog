package com.musiclog.social;

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
@Table(name = "activity_entries")
public class ActivityEntry {

    @Id
    private UUID id;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private ActivityType activityType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "target_mbid", length = 64)
    private String targetMbid;

    @Column(name = "target_type", length = 30)
    private String targetType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ActivityEntry() {
    }

    public ActivityEntry(UUID actorId, ActivityType activityType, UUID targetId, String targetMbid, String targetType) {
        this.id = UUID.randomUUID();
        this.actorId = actorId;
        this.activityType = activityType;
        this.targetId = targetId;
        this.targetMbid = targetMbid;
        this.targetType = targetType;
        this.createdAt = Instant.now();
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetMbid() {
        return targetMbid;
    }

    public String getTargetType() {
        return targetType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
