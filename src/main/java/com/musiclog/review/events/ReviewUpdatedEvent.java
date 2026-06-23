package com.musiclog.review.events;

import java.util.UUID;

public record ReviewUpdatedEvent(UUID reviewId, UUID userId) {
}
