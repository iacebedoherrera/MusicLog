package com.musiclog.review.dto;

import com.musiclog.review.ListeningLog;
import com.musiclog.review.ListeningSource;
import java.time.Instant;
import java.util.UUID;

public record ListeningLogResponse(
        UUID id,
        UUID userId,
        String trackMbid,
        Instant listenedAt,
        ListeningSource source
) {
    public static ListeningLogResponse from(ListeningLog log) {
        return new ListeningLogResponse(log.getId(), log.getUserId(), log.getTrackMbid(), log.getListenedAt(), log.getSource());
    }
}
