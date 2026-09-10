package com.musiclog.catalog;

import com.musiclog.shared.config.MusicBrainzProperties;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class MusicBrainzClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
    };
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final int MAX_RETRIES = 2;
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(1);

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
        try {
            return Mono.defer(() -> {
                awaitRateLimit();
                return webClient.get()
                        .uri(uriFunction)
                        .retrieve()
                        .bodyToMono(MAP_TYPE)
                        .switchIfEmpty(Mono.error(new IllegalStateException("MusicBrainz returned an empty response")))
                        .timeout(REQUEST_TIMEOUT);
            })
                    .retryWhen(Retry.backoff(MAX_RETRIES, INITIAL_RETRY_DELAY)
                            .maxBackoff(Duration.ofSeconds(4))
                            .filter(MusicBrainzClient::isRetryable)
                            .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                    .block();
        } catch (RuntimeException exception) {
            Throwable cause = Exceptions.unwrap(exception);
            if (cause instanceof WebClientResponseException
                    || cause instanceof WebClientRequestException
                    || cause instanceof TimeoutException) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "No se ha podido consultar el catálogo musical. Inténtalo de nuevo.",
                        cause);
            }
            throw exception;
        }
    }

    private static boolean isRetryable(Throwable exception) {
        if (exception instanceof WebClientResponseException responseException) {
            int status = responseException.getStatusCode().value();
            return status == 429 || responseException.getStatusCode().is5xxServerError();
        }
        return exception instanceof WebClientRequestException
                || exception instanceof TimeoutException;
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
