package com.musiclog.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
        @Min(1) @Max(10) Integer rating,
        @Size(max = 10000) String reviewText,
        Boolean containsSpoilers
) {
}
