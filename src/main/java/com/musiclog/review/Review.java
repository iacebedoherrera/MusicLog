package com.musiclog.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "target_mbid", nullable = false, length = 64)
    private String targetMbid;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReviewTargetType targetType;

    private Integer rating;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "contains_spoilers", nullable = false)
    private boolean containsSpoilers;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Review() {
    }

    public Review(UUID userId, String targetMbid, ReviewTargetType targetType, Integer rating, String reviewText, boolean containsSpoilers) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.targetMbid = targetMbid;
        this.targetType = targetType;
        this.rating = rating;
        this.reviewText = reviewText;
        this.containsSpoilers = containsSpoilers;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void update(Integer rating, String reviewText, Boolean containsSpoilers) {
        this.rating = rating;
        this.reviewText = reviewText;
        if (containsSpoilers != null) {
            this.containsSpoilers = containsSpoilers;
        }
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTargetMbid() {
        return targetMbid;
    }

    public ReviewTargetType getTargetType() {
        return targetType;
    }

    public Integer getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public boolean isContainsSpoilers() {
        return containsSpoilers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
