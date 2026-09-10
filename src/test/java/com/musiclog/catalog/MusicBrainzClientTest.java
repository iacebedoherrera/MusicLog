package com.musiclog.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.musiclog.shared.config.MusicBrainzProperties;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class MusicBrainzClientTest {

    @Test
    void retriesTransientProviderFailuresBeforeReturningResults() {
        AtomicInteger calls = new AtomicInteger();
        WebClient.Builder webClientBuilder = WebClient.builder()
                .exchangeFunction(request -> {
                    if (calls.getAndIncrement() < 2) {
                        return Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build());
                    }
                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .body("{\"count\":1,\"artists\":[{\"id\":\"artist-1\",\"name\":\"Radiohead\"}]}")
                            .build());
                });
        MusicBrainzClient client = new MusicBrainzClient(webClientBuilder, properties());

        Map<String, Object> response = client.search("Radiohead", "artist", 0, 1);

        assertThat(response).containsEntry("count", 1);
        assertThat(calls).hasValue(3);
    }

    private MusicBrainzProperties properties() {
        return new MusicBrainzProperties(
                "https://musicbrainz.org/ws/2",
                "https://coverartarchive.org",
                "MusicLog/0.1 (contacto@example.com)");
    }
}
