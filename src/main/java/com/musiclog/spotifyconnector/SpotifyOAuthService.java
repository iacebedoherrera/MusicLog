package com.musiclog.spotifyconnector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclog.shared.config.SpotifyProperties;
import com.musiclog.spotifyconnector.SpotifyApiClient.SpotifyTokenResponse;
import com.musiclog.spotifyconnector.dto.SpotifyStatusResponse;
import com.musiclog.spotifyconnector.dto.SpotifyTopResponse;
import jakarta.persistence.EntityNotFoundException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpotifyOAuthService {

    private static final Logger log = LoggerFactory.getLogger(SpotifyOAuthService.class);

    private final SpotifyConnectionRepository connectionRepository;
    private final SpotifyApiClient spotifyApiClient;
    private final TokenCipher tokenCipher;
    private final SpotifyProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SpotifyOAuthService(
            SpotifyConnectionRepository connectionRepository,
            SpotifyApiClient spotifyApiClient,
            TokenCipher tokenCipher,
            SpotifyProperties properties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.connectionRepository = connectionRepository;
        this.spotifyApiClient = spotifyApiClient;
        this.tokenCipher = tokenCipher;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public URI authorizationUrl(UUID userId) {
        return spotifyApiClient.authorizationUrl(userId.toString());
    }

    @Transactional
    public void handleCallback(String code, String state) {
        UUID userId = UUID.fromString(state);
        SpotifyTokenResponse tokenResponse = spotifyApiClient.exchangeCode(code);
        Map<String, Object> profile = spotifyApiClient.currentUser(tokenResponse.accessToken());
        String spotifyUserId = String.valueOf(profile.get("id"));
        String encryptedAccessToken = tokenCipher.encrypt(tokenResponse.accessToken());
        String encryptedRefreshToken = tokenCipher.encrypt(tokenResponse.refreshToken());
        SpotifyConnection connection = connectionRepository.findByUserId(userId)
                .orElseGet(() -> new SpotifyConnection(userId, spotifyUserId, encryptedAccessToken, encryptedRefreshToken, tokenResponse.expiresAt()));
        connection.updateTokens(encryptedAccessToken, encryptedRefreshToken, tokenResponse.expiresAt());
        connectionRepository.save(connection);
    }

    @Transactional
    public void disconnect(UUID userId) {
        connectionRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public SpotifyStatusResponse status(UUID userId) {
        return connectionRepository.findByUserId(userId)
                .map(connection -> new SpotifyStatusResponse(
                        true,
                        connection.getSpotifyUserId(),
                        connection.getConnectedAt(),
                        connection.getLastSyncAt()))
                .orElseGet(SpotifyStatusResponse::disconnected);
    }

    @Transactional
    public String accessToken(SpotifyConnection connection) {
        if (connection.getTokenExpiresAt().isAfter(Instant.now().plusSeconds(60))) {
            return tokenCipher.decrypt(connection.getAccessToken());
        }
        String refreshToken = tokenCipher.decrypt(connection.getRefreshToken());
        SpotifyTokenResponse refreshed = spotifyApiClient.refresh(refreshToken);
        connection.updateTokens(
                tokenCipher.encrypt(refreshed.accessToken()),
                tokenCipher.encrypt(refreshed.refreshToken()),
                refreshed.expiresAt());
        connectionRepository.save(connection);
        return refreshed.accessToken();
    }

    public SpotifyTopResponse cachedTopArtists(UUID userId) {
        return cachedTop("spotify:top:artists:" + userId);
    }

    public SpotifyTopResponse cachedTopTracks(UUID userId) {
        return cachedTop("spotify:top:tracks:" + userId);
    }

    public String frontendRedirectUri() {
        return properties.frontendRedirectUri();
    }

    private SpotifyTopResponse cachedTop(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? SpotifyTopResponse.empty() : objectMapper.readValue(json, SpotifyTopResponse.class);
        } catch (RedisConnectionFailureException | JsonProcessingException exception) {
            log.debug("Could not read Spotify top cache {}", key, exception);
            return SpotifyTopResponse.empty();
        }
    }

    @Transactional(readOnly = true)
    public SpotifyConnection requireConnection(UUID userId) {
        return connectionRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Spotify connection not found"));
    }
}
