package com.musiclog.spotifyconnector;

import com.musiclog.shared.security.AuthenticatedUser;
import com.musiclog.spotifyconnector.dto.SpotifyStatusResponse;
import com.musiclog.spotifyconnector.dto.SpotifyTopResponse;
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
public class SpotifyController {

    private final SpotifyOAuthService spotifyOAuthService;
    private final SpotifySyncJob spotifySyncJob;

    public SpotifyController(SpotifyOAuthService spotifyOAuthService, SpotifySyncJob spotifySyncJob) {
        this.spotifyOAuthService = spotifyOAuthService;
        this.spotifySyncJob = spotifySyncJob;
    }

    @GetMapping("/api/spotify/connect")
    public RedirectView connect(Authentication authentication) {
        UUID userId = AuthenticatedUser.from(authentication).id();
        return new RedirectView(spotifyOAuthService.authorizationUrl(userId).toString());
    }

    @GetMapping("/api/spotify/callback")
    public RedirectView callback(@RequestParam String code, @RequestParam String state) {
        spotifyOAuthService.handleCallback(code, state);
        return new RedirectView(spotifyOAuthService.frontendRedirectUri());
    }

    @DeleteMapping("/api/spotify/disconnect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(Authentication authentication) {
        spotifyOAuthService.disconnect(AuthenticatedUser.from(authentication).id());
    }

    @GetMapping("/api/spotify/status")
    public SpotifyStatusResponse status(Authentication authentication) {
        return spotifyOAuthService.status(AuthenticatedUser.from(authentication).id());
    }

    @GetMapping("/api/spotify/top/artists")
    public SpotifyTopResponse topArtists(Authentication authentication) {
        return spotifyOAuthService.cachedTopArtists(AuthenticatedUser.from(authentication).id());
    }

    @GetMapping("/api/spotify/top/tracks")
    public SpotifyTopResponse topTracks(Authentication authentication) {
        return spotifyOAuthService.cachedTopTracks(AuthenticatedUser.from(authentication).id());
    }

    @PostMapping("/api/spotify/sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sync(Authentication authentication) {
        spotifySyncJob.syncUser(AuthenticatedUser.from(authentication).id());
    }
}
