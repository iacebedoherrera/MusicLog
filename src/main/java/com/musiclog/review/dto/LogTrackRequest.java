package com.musiclog.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Schema(description = "Escucha manual de una pista.")
public record LogTrackRequest(
        @Schema(description = "MusicBrainz ID de la pista", example = "a0f6f1c5-8f4f-4f5c-9d9a-a5c8c60f7f20") @NotBlank String trackMbid,
        @Schema(description = "Fecha y hora ISO-8601; si se omite se usa el instante actual", example = "2026-07-15T18:30:00Z") Instant listenedAt
) {
}
