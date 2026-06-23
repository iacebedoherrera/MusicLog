package com.musiclog.user.events;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String username) {
}
