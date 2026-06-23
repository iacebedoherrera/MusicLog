package com.musiclog.review.events;

import com.musiclog.review.ReviewTargetType;
import java.util.UUID;

public record ReviewCreatedEvent(
        UUID reviewId,
        UUID userId,
        String targetMbid,
        ReviewTargetType targetType,
        Integer rating
) {
}
