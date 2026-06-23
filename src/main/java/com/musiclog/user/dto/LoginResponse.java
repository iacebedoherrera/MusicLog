package com.musiclog.user.dto;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID userId,
        String username
) {
}
