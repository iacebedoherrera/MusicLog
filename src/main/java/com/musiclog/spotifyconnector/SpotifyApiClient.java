package com.musiclog.spotifyconnector;

import com.musiclog.shared.config.SpotifyProperties;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SpotifyApiClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    private final SpotifyProperties properties;
    private final WebClient accountsClient;
    private final WebClient apiClient;

    public SpotifyApiClient(WebClient.Builder webClientBuilder, SpotifyProperties properties) {
        this.properties = properties;
        this.accountsClient = webClientBuilder.clone().baseUrl("https://accounts.spotify.com").build();
        this.apiClient = webClientBuilder.clone().baseUrl("https://api.spotify.com/v1").build();
    }

    public URI authorizationUrl(String state) {
        return UriComponentsBuilder.fromUriString("https://accounts.spotify.com/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("scope", "user-read-recently-played user-top-read")
                .queryParam("state", state)
                .build()
                .toUri();
    }

    public SpotifyTokenResponse exchangeCode(String code) {
        Map<String, Object> payload = accountsClient.post()
                .uri("/api/token")
                .headers(headers -> headers.setBasicAuth(properties.clientId(), properties.clientSecret()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("code", code)
                        .with("redirect_uri", properties.redirectUri()))
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .block();
        return tokenResponse(payload);
    }

    public SpotifyTokenResponse refresh(String refreshToken) {
        Map<String, Object> payload = accountsClient.post()
                .uri("/api/token")
                .headers(headers -> headers.setBasicAuth(properties.clientId(), properties.clientSecret()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .block();
        SpotifyTokenResponse response = tokenResponse(payload);
        return new SpotifyTokenResponse(response.accessToken(), response.refreshToken() == null ? refreshToken : response.refreshToken(), response.expiresIn());
    }

    public Map<String, Object> currentUser(String accessToken) {
        return get(accessToken, "/me");
    }

    public Map<String, Object> recentlyPlayed(String accessToken) {
        return apiClient.get()
                .uri(uri -> uri.path("/me/player/recently-played").queryParam("limit", 50).build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .block();
    }

    public Map<String, Object> topArtists(String accessToken) {
        return apiClient.get()
                .uri(uri -> uri.path("/me/top/artists").queryParam("time_range", "short_term").build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .block();
    }

    public Map<String, Object> topTracks(String accessToken) {
        return apiClient.get()
                .uri(uri -> uri.path("/me/top/tracks").queryParam("time_range", "short_term").build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .block();
    }

    private Map<String, Object> get(String accessToken, String path) {
        return apiClient.get()
                .uri(path)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .block();
    }

    private SpotifyTokenResponse tokenResponse(Map<String, Object> payload) {
        if (payload == null) {
            throw new IllegalStateException("Spotify returned an empty token response");
        }
        return new SpotifyTokenResponse(
                (String) payload.get("access_token"),
                (String) payload.get("refresh_token"),
                number(payload.get("expires_in"), 3600));
    }

    private long number(Object value, long defaultValue) {
        return value instanceof Number number ? number.longValue() : defaultValue;
    }

    public record SpotifyTokenResponse(String accessToken, String refreshToken, long expiresIn) {
        public Instant expiresAt() {
            return Instant.now().plusSeconds(expiresIn);
        }
    }
}
