package com.musiclog.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "review_likes")
public class ReviewLike {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReviewLike() {
    }

    public ReviewLike(UUID userId, UUID reviewId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.reviewId = reviewId;
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

    public UUID getUserId() {
        return userId;
    }

    public UUID getReviewId() {
        return reviewId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
