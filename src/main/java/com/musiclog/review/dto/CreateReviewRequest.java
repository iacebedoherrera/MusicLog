package com.musiclog.review.dto;

import com.musiclog.review.ReviewTargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotBlank String targetMbid,
        @NotNull ReviewTargetType targetType,
        @Min(1) @Max(10) Integer rating,
        @Size(max = 10000) String reviewText,
        boolean containsSpoilers
) {
}
