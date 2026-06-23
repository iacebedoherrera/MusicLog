package com.musiclog.review.events;

import com.musiclog.review.ListeningSource;
import java.time.Instant;
import java.util.UUID;

public record TrackLoggedEvent(UUID userId, String trackMbid, Instant listenedAt, ListeningSource source) {
}
