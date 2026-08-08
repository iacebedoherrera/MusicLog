package com.musiclog.review.dto;

import com.musiclog.review.Review;
import com.musiclog.review.ReviewTargetType;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID userId,
        ReviewAuthorResponse author,
        String targetMbid,
        ReviewTargetType targetType,
        Integer rating,
        String reviewText,
        boolean containsSpoilers,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReviewResponse from(Review review, ReviewAuthorResponse author) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                author,
                review.getTargetMbid(),
                review.getTargetType(),
                review.getRating(),
                review.getReviewText(),
                review.isContainsSpoilers(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
