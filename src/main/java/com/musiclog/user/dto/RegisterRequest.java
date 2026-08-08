package com.musiclog.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos requeridos para crear una cuenta.")
public record RegisterRequest(
        @Schema(description = "Identificador público único", example = "ana_music") @NotBlank @Size(max = 50) String username,
        @Schema(description = "Correo electrónico de la cuenta", example = "ana@example.com") @NotBlank @Email @Size(max = 255) String email,
        @Schema(description = "Contraseña de 8 a 100 caracteres", example = "mi-contraseña-segura", format = "password") @NotBlank @Size(min = 8, max = 100) String password,
        @Schema(description = "Nombre mostrado en el perfil", example = "Ana") @NotBlank @Size(max = 100) String displayName
) {
}
