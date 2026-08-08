package com.musiclog.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos editables de una reseña. Los campos omitidos conservan su valor actual.")
public record UpdateReviewRequest(
        @Schema(description = "Puntuación de 1 a 10", example = "10", minimum = "1", maximum = "10") @Min(1) @Max(10) Integer rating,
        @Schema(example = "Tras varias escuchas, aún mejor.") @Size(max = 10000) String reviewText,
        @Schema(example = "false") Boolean containsSpoilers
) {
}
