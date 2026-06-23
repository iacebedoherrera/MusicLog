package com.musiclog.user.events;

import java.util.UUID;

public record UserUnfollowedEvent(UUID followerId, UUID followedId) {
}
