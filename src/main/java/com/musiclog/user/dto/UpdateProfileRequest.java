package com.musiclog.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100) String displayName,
        @Size(max = 2000) String bio,
        @Size(max = 500) String avatarUrl
) {
}
