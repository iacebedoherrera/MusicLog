package com.musiclog.social.dto;

import java.util.List;

public record FeedResponse(
        List<ActivityResponse> activities,
        int page,
        int size
) {
}
