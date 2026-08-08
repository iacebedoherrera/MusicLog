package com.musiclog.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de acceso.")
public record LoginRequest(
        @Schema(description = "Nombre de usuario o correo electrónico", example = "ana_music") @NotBlank String usernameOrEmail,
        @Schema(description = "Contraseña de la cuenta", example = "mi-contraseña-segura", format = "password") @NotBlank String password
) {
}
