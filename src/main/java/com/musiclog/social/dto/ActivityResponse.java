package com.musiclog.social.dto;

import com.musiclog.social.ActivityEntry;
import com.musiclog.social.ActivityType;
import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID actorId,
        ActivityType activityType,
        UUID targetId,
        String targetMbid,
        String targetType,
        Instant createdAt
) {
    public static ActivityResponse from(ActivityEntry activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActorId(),
                activity.getActivityType(),
                activity.getTargetId(),
                activity.getTargetMbid(),
                activity.getTargetType(),
                activity.getCreatedAt());
    }
}
