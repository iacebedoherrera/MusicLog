package com.musiclog.user;

import com.musiclog.shared.security.AuthenticatedUser;
import com.musiclog.user.dto.UpdateProfileRequest;
import com.musiclog.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Perfiles públicos y relaciones de seguimiento entre usuarios.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    @Operation(summary = "Obtener perfil público")
    public UserProfileResponse profile(@PathVariable String username) {
        return userService.getPublicProfile(username);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener mi perfil", description = "Recupera el perfil del usuario autenticado para restaurar una sesión.")
    @SecurityRequirement(name = "bearerAuth")
    public UserProfileResponse me(Authentication authentication) {
        return userService.getProfile(AuthenticatedUser.from(authentication).id());
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar mi perfil")
    @SecurityRequirement(name = "bearerAuth")
    public UserProfileResponse updateMe(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(AuthenticatedUser.from(authentication).id(), request);
    }

    @PostMapping("/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Seguir a un usuario")
    @SecurityRequirement(name = "bearerAuth")
    public void follow(Authentication authentication, @PathVariable String username) {
        userService.follow(AuthenticatedUser.from(authentication).id(), username);
    }

    @DeleteMapping("/{username}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Dejar de seguir a un usuario")
    @SecurityRequirement(name = "bearerAuth")
    public void unfollow(Authentication authentication, @PathVariable String username) {
        userService.unfollow(AuthenticatedUser.from(authentication).id(), username);
    }

    @GetMapping("/{username}/followers")
    @Operation(summary = "Listar seguidores de un usuario")
    public List<UserProfileResponse> followers(@PathVariable String username) {
        return userService.followers(username);
    }

    @GetMapping("/{username}/following")
    @Operation(summary = "Listar usuarios seguidos")
    public List<UserProfileResponse> following(@PathVariable String username) {
        return userService.following(username);
    }
}
