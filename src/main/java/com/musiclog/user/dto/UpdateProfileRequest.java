package com.musiclog.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos editables del perfil. Los campos omitidos conservan su valor actual.")
public record UpdateProfileRequest(
        @Schema(example = "Ana") @Size(max = 100) String displayName,
        @Schema(example = "Escucho de todo, especialmente post-rock.") @Size(max = 2000) String bio,
        @Schema(description = "URL pública de la imagen de perfil", example = "https://example.com/avatar.jpg") @Size(max = 500) String avatarUrl
) {
}
