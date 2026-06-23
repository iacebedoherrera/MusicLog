package com.musiclog.social.dto;

import java.util.UUID;

public record LikeResponse(
        UUID reviewId,
        long likes,
        boolean likedByCurrentUser
) {
}
