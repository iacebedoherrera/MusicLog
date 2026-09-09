package com.musiclog.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ValidAccountPasswordPair
@Schema(description = "Datos de identidad y contraseña del usuario autenticado.")
public record UpdateAccountRequest(
        @Schema(description = "Nombre mostrado", example = "Ana")
        @NotBlank
        @Size(max = 100)
        String displayName,
        @Schema(description = "Nombre de usuario público", example = "ana_music")
        @NotBlank
        @Size(max = 50)
        String username,
        @Schema(description = "Nueva contraseña opcional. Debe enviarse junto con su confirmación.", format = "password", minLength = 8, maxLength = 100)
        @Size(max = 100)
        String newPassword,
        @Schema(description = "Confirmación de la nueva contraseña opcional.", format = "password", minLength = 8, maxLength = 100)
        @Size(max = 100)
        String newPasswordConfirmation
) {
}
