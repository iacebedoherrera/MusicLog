package com.musiclog.catalog;

import com.musiclog.shared.config.MusicBrainzProperties;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Component
public class MusicBrainzClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    private final WebClient webClient;
    private long nextAllowedRequestAt;

    public MusicBrainzClient(WebClient.Builder webClientBuilder, MusicBrainzProperties properties) {
        this.webClient = webClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader("User-Agent", properties.userAgent())
                .build();
    }

    public Map<String, Object> search(String query, String type, int page, int size) {
        String endpoint = switch (type) {
            case "artist" -> "/artist";
            case "album" -> "/release";
            case "track" -> "/recording";
            default -> throw new IllegalArgumentException("Unsupported search type: " + type);
        };
        return get(uri -> uri.path(endpoint)
                .queryParam("query", query)
                .queryParam("limit", size)
                .queryParam("offset", (long) page * size)
                .queryParam("fmt", "json")
                .build());
    }

    public Map<String, Object> artist(String mbid) {
        return get(uri -> uri.path("/artist/{mbid}")
                .queryParam("fmt", "json")
                .build(mbid));
    }

    public Map<String, Object> album(String mbid) {
        return get(uri -> uri.path("/release/{mbid}")
                .queryParam("inc", "artist-credits+release-groups")
                .queryParam("fmt", "json")
                .build(mbid));
    }

    public Map<String, Object> track(String mbid) {
        return get(uri -> uri.path("/recording/{mbid}")
                .queryParam("inc", "artists+releases+isrcs")
                .queryParam("fmt", "json")
                .build(mbid));
    }

    public Map<String, Object> artistAlbums(String mbid) {
        return get(uri -> uri.path("/release")
                .queryParam("artist", mbid)
                .queryParam("limit", 50)
                .queryParam("fmt", "json")
                .build());
    }

    private Map<String, Object> get(Function<UriBuilder, URI> uriFunction) {
        awaitRateLimit();
        return webClient.get()
                .uri(uriFunction)
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .block();
    }

    private synchronized void awaitRateLimit() {
        long now = System.currentTimeMillis();
        long waitMs = nextAllowedRequestAt - now;
        if (waitMs > 0) {
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while respecting MusicBrainz rate limit", exception);
            }
        }
        nextAllowedRequestAt = System.currentTimeMillis() + 1000;
    }
}
