package com.musiclog.spotifyconnector;

import com.musiclog.shared.security.AuthenticatedUser;
import com.musiclog.spotifyconnector.dto.SpotifyStatusResponse;
import com.musiclog.spotifyconnector.dto.SpotifyTopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Tag(name = "Spotify", description = "Conexión OAuth y consulta de información sincronizada de Spotify.")
public class SpotifyController {

    private final SpotifyOAuthService spotifyOAuthService;
    private final SpotifySyncJob spotifySyncJob;

    public SpotifyController(SpotifyOAuthService spotifyOAuthService, SpotifySyncJob spotifySyncJob) {
        this.spotifyOAuthService = spotifyOAuthService;
        this.spotifySyncJob = spotifySyncJob;
    }

    @GetMapping("/api/spotify/connect")
    @Operation(summary = "Iniciar la conexión con Spotify", description = "Redirige al consentimiento OAuth de Spotify.")
    @SecurityRequirement(name = "bearerAuth")
    public RedirectView connect(Authentication authentication) {
        UUID userId = AuthenticatedUser.from(authentication).id();
        return new RedirectView(spotifyOAuthService.authorizationUrl(userId).toString());
    }

    @GetMapping("/api/spotify/callback")
    @Operation(summary = "Recibir el callback OAuth de Spotify", description = "Endpoint de redirección usado por Spotify; no se invoca manualmente.")
    public RedirectView callback(@RequestParam String code, @RequestParam String state) {
        spotifyOAuthService.handleCallback(code, state);
        return new RedirectView(spotifyOAuthService.frontendRedirectUri());
    }

    @DeleteMapping("/api/spotify/disconnect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desconectar Spotify")
    @SecurityRequirement(name = "bearerAuth")
    public void disconnect(Authentication authentication) {
        spotifyOAuthService.disconnect(AuthenticatedUser.from(authentication).id());
    }

    @GetMapping("/api/spotify/status")
    @Operation(summary = "Consultar el estado de Spotify")
    @SecurityRequirement(name = "bearerAuth")
    public SpotifyStatusResponse status(Authentication authentication) {
        return spotifyOAuthService.status(AuthenticatedUser.from(authentication).id());
    }

    @GetMapping("/api/spotify/top/artists")
    @Operation(summary = "Obtener mis artistas más escuchados sincronizados")
    @SecurityRequirement(name = "bearerAuth")
    public SpotifyTopResponse topArtists(Authentication authentication) {
        return spotifyOAuthService.cachedTopArtists(AuthenticatedUser.from(authentication).id());
    }

    @GetMapping("/api/spotify/top/tracks")
    @Operation(summary = "Obtener mis pistas más escuchadas sincronizadas")
    @SecurityRequirement(name = "bearerAuth")
    public SpotifyTopResponse topTracks(Authentication authentication) {
        return spotifyOAuthService.cachedTopTracks(AuthenticatedUser.from(authentication).id());
    }

    @PostMapping("/api/spotify/sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Solicitar una sincronización con Spotify", description = "Inicia la sincronización de forma asíncrona.")
    @SecurityRequirement(name = "bearerAuth")
    public void sync(Authentication authentication) {
        spotifySyncJob.syncUser(AuthenticatedUser.from(authentication).id());
    }
}
