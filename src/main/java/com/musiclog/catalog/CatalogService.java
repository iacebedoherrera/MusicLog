package com.musiclog.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclog.catalog.dto.AlbumResponse;
import com.musiclog.catalog.dto.ArtistResponse;
import com.musiclog.catalog.dto.SearchResponse;
import com.musiclog.catalog.dto.TrackResponse;
import com.musiclog.shared.config.MusicBrainzProperties;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
    private static final Duration CATALOG_TTL = Duration.ofHours(24);

    private final CachedArtistRepository artistRepository;
    private final CachedAlbumRepository albumRepository;
    private final CachedTrackRepository trackRepository;
    private final MusicBrainzClient musicBrainzClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MusicBrainzProperties properties;

    public CatalogService(
            CachedArtistRepository artistRepository,
            CachedAlbumRepository albumRepository,
            CachedTrackRepository trackRepository,
            MusicBrainzClient musicBrainzClient,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            MusicBrainzProperties properties) {
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.musicBrainzClient = musicBrainzClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public SearchResponse search(String query, String type) {
        Map<String, Object> payload = musicBrainzClient.search(query, type);
        return new SearchResponse(type, searchItems(payload, type));
    }

    @Transactional
    public ArtistResponse artist(String mbid) {
        String cacheKey = "catalog:artist:" + mbid;
        Optional<ArtistResponse> fromRedis = read(cacheKey, ArtistResponse.class);
        if (fromRedis.isPresent()) {
            return fromRedis.get();
        }
        Optional<ArtistResponse> fromDb = artistRepository.findById(mbid).map(ArtistResponse::from);
        if (fromDb.isPresent()) {
            write(cacheKey, fromDb.get());
            return fromDb.get();
        }
        CachedArtist cached = toArtist(musicBrainzClient.artist(mbid));
        ArtistResponse response = ArtistResponse.from(artistRepository.save(cached));
        write(cacheKey, response);
        return response;
    }

    @Transactional
    public AlbumResponse album(String mbid) {
        String cacheKey = "catalog:album:" + mbid;
        Optional<AlbumResponse> fromRedis = read(cacheKey, AlbumResponse.class);
        if (fromRedis.isPresent()) {
            return fromRedis.get();
        }
        Optional<AlbumResponse> fromDb = albumRepository.findById(mbid).map(AlbumResponse::from);
        if (fromDb.isPresent()) {
            write(cacheKey, fromDb.get());
            return fromDb.get();
        }
        CachedAlbum cached = toAlbum(musicBrainzClient.album(mbid));
        AlbumResponse response = AlbumResponse.from(albumRepository.save(cached));
        write(cacheKey, response);
        return response;
    }

    @Transactional
    public TrackResponse track(String mbid) {
        String cacheKey = "catalog:track:" + mbid;
        Optional<TrackResponse> fromRedis = read(cacheKey, TrackResponse.class);
        if (fromRedis.isPresent()) {
            return fromRedis.get();
        }
        Optional<TrackResponse> fromDb = trackRepository.findById(mbid).map(TrackResponse::from);
        if (fromDb.isPresent()) {
            write(cacheKey, fromDb.get());
            return fromDb.get();
        }
        CachedTrack cached = toTrack(musicBrainzClient.track(mbid));
        TrackResponse response = TrackResponse.from(trackRepository.save(cached));
        write(cacheKey, response);
        return response;
    }

    @Transactional
    public List<AlbumResponse> artistAlbums(String artistMbid) {
        List<CachedAlbum> cachedAlbums = albumRepository.findByArtistMbidOrderByReleaseDateDesc(artistMbid);
        if (!cachedAlbums.isEmpty()) {
            return cachedAlbums.stream().map(AlbumResponse::from).toList();
        }
        Map<String, Object> payload = musicBrainzClient.artistAlbums(artistMbid);
        List<Map<String, Object>> releases = list(payload.get("releases"));
        List<CachedAlbum> albums = releases.stream()
                .map(this::toAlbum)
                .filter(album -> album.getMbid() != null)
                .map(albumRepository::save)
                .toList();
        return albums.stream().map(AlbumResponse::from).toList();
    }

    public Optional<String> findTrackMbidByIsrcOrTitleArtist(String isrc, String title, String artist) {
        String query;
        if (isrc != null && !isrc.isBlank()) {
            query = "isrc:" + isrc;
        } else if (title != null && artist != null) {
            query = "recording:\"" + title + "\" AND artist:\"" + artist + "\"";
        } else {
            return Optional.empty();
        }
        SearchResponse response = search(query, "track");
        return response.results().stream().findFirst().map(SearchResponse.SearchItem::mbid);
    }

    private List<SearchResponse.SearchItem> searchItems(Map<String, Object> payload, String type) {
        String key = switch (type) {
            case "artist" -> "artists";
            case "album" -> "releases";
            case "track" -> "recordings";
            default -> throw new IllegalArgumentException("Unsupported search type: " + type);
        };
        return list(payload.get(key)).stream()
                .map(item -> new SearchResponse.SearchItem(
                        string(item, "id"),
                        title(item, type),
                        subtitle(item, type),
                        integer(item.get("score"))))
                .toList();
    }

    private CachedArtist toArtist(Map<String, Object> payload) {
        return new CachedArtist(
                string(payload, "id"),
                string(payload, "name"),
                string(payload, "sort-name"),
                string(payload, "country"),
                string(payload, "disambiguation"));
    }

    private CachedAlbum toAlbum(Map<String, Object> payload) {
        String mbid = string(payload, "id");
        String artistMbid = firstArtistMbid(payload);
        String type = null;
        Object releaseGroup = payload.get("release-group");
        if (releaseGroup instanceof Map<?, ?> group) {
            type = valueAsString(group.get("primary-type"));
        }
        return new CachedAlbum(
                mbid,
                string(payload, "title"),
                artistMbid,
                string(payload, "date"),
                type,
                mbid == null ? null : properties.coverArtBaseUrl() + "/release/" + mbid + "/front-250");
    }

    private CachedTrack toTrack(Map<String, Object> payload) {
        return new CachedTrack(
                string(payload, "id"),
                string(payload, "title"),
                firstReleaseMbid(payload),
                firstArtistMbid(payload),
                integer(payload.get("length")),
                null);
    }

    private String title(Map<String, Object> item, String type) {
        return "artist".equals(type) ? string(item, "name") : string(item, "title");
    }

    private String subtitle(Map<String, Object> item, String type) {
        if ("artist".equals(type)) {
            return string(item, "disambiguation");
        }
        if ("album".equals(type)) {
            return firstArtistName(item);
        }
        return firstArtistName(item);
    }

    private String firstArtistMbid(Map<String, Object> payload) {
        return firstArtistField(payload, "id");
    }

    private String firstArtistName(Map<String, Object> payload) {
        return firstArtistField(payload, "name");
    }

    private String firstArtistField(Map<String, Object> payload, String field) {
        List<Map<String, Object>> credits = list(payload.get("artist-credit"));
        if (credits.isEmpty()) {
            return null;
        }
        Object artist = credits.getFirst().get("artist");
        if (artist instanceof Map<?, ?> artistMap) {
            return valueAsString(artistMap.get(field));
        }
        return null;
    }

    private String firstReleaseMbid(Map<String, Object> payload) {
        List<Map<String, Object>> releases = list(payload.get("releases"));
        return releases.isEmpty() ? null : string(releases.getFirst(), "id");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        if (value instanceof List<?> rawList) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        }
        return List.of();
    }

    private String string(Map<String, Object> payload, String key) {
        return valueAsString(payload.get(key));
    }

    private String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string && !string.isBlank()) {
            return Integer.parseInt(string);
        }
        return null;
    }

    private <T> Optional<T> read(String key, Class<T> type) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(value, type));
        } catch (RedisConnectionFailureException | JsonProcessingException exception) {
            log.debug("Catalog Redis read miss for {}", key, exception);
            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), CATALOG_TTL);
        } catch (RedisConnectionFailureException | JsonProcessingException exception) {
            log.debug("Catalog Redis write skipped for {}", key, exception);
        }
    }
}
