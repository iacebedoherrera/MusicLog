package com.musiclog.spotifyconnector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclog.shared.config.SpotifyProperties;
import com.musiclog.spotifyconnector.dto.SpotifyStatusResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

class SpotifyOAuthServiceTest {

    @Test
    void statusIsDisconnectedWhenNoConnectionExists() {
        SpotifyConnectionRepository repository = mock(SpotifyConnectionRepository.class);
        when(repository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"))).thenReturn(Optional.empty());
        SpotifyProperties properties = new SpotifyProperties(
                "client",
                "secret",
                "http://localhost/callback",
                "encryption-key",
                21600000,
                21600000,
                "http://localhost/frontend");
        SpotifyOAuthService service = new SpotifyOAuthService(
                repository,
                mock(SpotifyApiClient.class),
                new TokenCipher(properties),
                properties,
                mock(StringRedisTemplate.class),
                new ObjectMapper());

        SpotifyStatusResponse response = service.status(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThat(response.connected()).isFalse();
    }
}
