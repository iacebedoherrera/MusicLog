package com.musiclog.review.dto;

import com.musiclog.review.ReviewTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear una reseña. Debe incluir una puntuación, texto, o ambos.")
public record CreateReviewRequest(
        @Schema(description = "MusicBrainz ID del elemento reseñado", example = "a0f6f1c5-8f4f-4f5c-9d9a-a5c8c60f7f20") @NotBlank String targetMbid,
        @Schema(description = "Tipo de elemento musical", example = "ALBUM") @NotNull ReviewTargetType targetType,
        @Schema(description = "Puntuación de 1 a 10", example = "9", minimum = "1", maximum = "10") @Min(1) @Max(10) Integer rating,
        @Schema(description = "Texto de la reseña", example = "Un disco excelente.") @Size(max = 10000) String reviewText,
        @Schema(description = "Indica si el texto contiene spoilers", example = "false") boolean containsSpoilers
) {
}
