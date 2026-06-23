package com.musiclog.user.events;

import java.util.UUID;

public record UserFollowedEvent(UUID followerId, UUID followedId) {
}
