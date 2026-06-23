package com.musiclog.review.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record LogTrackRequest(
        @NotBlank String trackMbid,
        Instant listenedAt
) {
}
