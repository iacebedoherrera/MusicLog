package com.musiclog.review.dto;

import com.musiclog.user.dto.UserProfileResponse;
import java.util.UUID;

/** Public author data embedded in a review response. */
public record ReviewAuthorResponse(
        UUID id,
        String username,
        String displayName,
        String avatarUrl
) {
    public static ReviewAuthorResponse from(UserProfileResponse user) {
        return new ReviewAuthorResponse(user.id(), user.username(), user.displayName(), user.avatarUrl());
    }
}
