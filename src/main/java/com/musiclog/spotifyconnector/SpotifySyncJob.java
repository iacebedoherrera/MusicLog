package com.musiclog.spotifyconnector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclog.catalog.CatalogService;
import com.musiclog.review.ListeningSource;
import com.musiclog.review.ReviewService;
import com.musiclog.spotifyconnector.dto.SpotifyTopResponse;
import com.musiclog.spotifyconnector.dto.SpotifyTopResponse.SpotifyTopItem;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpotifySyncJob {

    private static final Logger log = LoggerFactory.getLogger(SpotifySyncJob.class);
    private static final Duration TOP_TTL = Duration.ofHours(6);

    private final SpotifyConnectionRepository connectionRepository;
    private final SpotifyOAuthService spotifyOAuthService;
    private final SpotifyApiClient spotifyApiClient;
    private final CatalogService catalogService;
    private final ReviewService reviewService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SpotifySyncJob(
            SpotifyConnectionRepository connectionRepository,
            SpotifyOAuthService spotifyOAuthService,
            SpotifyApiClient spotifyApiClient,
            CatalogService catalogService,
            ReviewService reviewService,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.connectionRepository = connectionRepository;
        this.spotifyOAuthService = spotifyOAuthService;
        this.spotifyApiClient = spotifyApiClient;
        this.catalogService = catalogService;
        this.reviewService = reviewService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(
            fixedDelayString = "${musiclog.spotify.sync-interval-ms:21600000}",
            initialDelayString = "${musiclog.spotify.sync-initial-delay-ms:21600000}")
    public void syncConnectedUsers() {
        connectionRepository.findAll().forEach(connection -> syncUser(connection.getUserId()));
    }

    @Transactional
    public void syncUser(UUID userId) {
        SpotifyConnection connection = spotifyOAuthService.requireConnection(userId);
        try {
            String accessToken = spotifyOAuthService.accessToken(connection);
            syncRecentlyPlayed(userId, accessToken);
            cacheTop(userId, "artists", spotifyApiClient.topArtists(accessToken));
            cacheTop(userId, "tracks", spotifyApiClient.topTracks(accessToken));
            connection.markSynced(Instant.now());
            connectionRepository.save(connection);
        } catch (RuntimeException exception) {
            log.warn("Spotify sync failed for user {}", userId, exception);
            throw exception;
        }
    }

    private void syncRecentlyPlayed(UUID userId, String accessToken) {
        Map<String, Object> payload = spotifyApiClient.recentlyPlayed(accessToken);
        for (Map<String, Object> item : list(payload.get("items"))) {
            Object trackObject = item.get("track");
            if (!(trackObject instanceof Map<?, ?> track)) {
                continue;
            }
            String title = value(track.get("name"));
            String artist = firstArtistName(track);
            String isrc = externalId(track, "isrc");
            Instant listenedAt = parseInstant(value(item.get("played_at"))).orElseGet(Instant::now);
            catalogService.findTrackMbidByIsrcOrTitleArtist(isrc, title, artist)
                    .ifPresent(mbid -> reviewService.logTrack(userId, mbid, listenedAt, ListeningSource.SPOTIFY_IMPORT));
        }
    }

    private void cacheTop(UUID userId, String type, Map<String, Object> payload) {
        SpotifyTopResponse response = new SpotifyTopResponse(topItems(payload), Instant.now());
        String key = "spotify:top:" + type + ":" + userId;
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(response), TOP_TTL);
        } catch (RedisConnectionFailureException | JsonProcessingException exception) {
            log.debug("Could not cache Spotify top {} for user {}", type, userId, exception);
        }
    }

    private List<SpotifyTopItem> topItems(Map<String, Object> payload) {
        return list(payload.get("items")).stream()
                .map(item -> new SpotifyTopItem(
                        value(item.get("id")),
                        value(item.get("name")),
                        firstArtistName(item),
                        integer(item.get("popularity"))))
                .toList();
    }

    private String firstArtistName(Map<?, ?> track) {
        List<Map<String, Object>> artists = list(track.get("artists"));
        if (artists.isEmpty()) {
            return null;
        }
        return value(artists.getFirst().get("name"));
    }

    private String externalId(Map<?, ?> track, String key) {
        Object externalIds = track.get("external_ids");
        if (externalIds instanceof Map<?, ?> map) {
            return value(map.get(key));
        }
        return null;
    }

    private Optional<Instant> parseInstant(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(Instant.parse(value));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return result;
    }

    private String value(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }
}
