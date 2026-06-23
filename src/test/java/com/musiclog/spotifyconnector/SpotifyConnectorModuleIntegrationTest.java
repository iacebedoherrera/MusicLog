package com.musiclog.spotifyconnector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.musiclog.SpringModulithIntegrationTest;
import com.musiclog.spotifyconnector.SpotifyApiClient.SpotifyTokenResponse;
import com.musiclog.spotifyconnector.dto.SpotifyStatusResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringModulithIntegrationTest
class SpotifyConnectorModuleIntegrationTest {

    @Autowired
    private SpotifyOAuthService spotifyOAuthService;

    @MockBean
    private SpotifyApiClient spotifyApiClient;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    void callbackStoresEncryptedConnectionAndStatusIsConnected() {
        UUID userId = UUID.randomUUID();
        when(spotifyApiClient.exchangeCode("code")).thenReturn(new SpotifyTokenResponse("access", "refresh", 3600));
        when(spotifyApiClient.currentUser("access")).thenReturn(Map.of("id", "spotify-" + userId));

        spotifyOAuthService.handleCallback("code", userId.toString());

        SpotifyStatusResponse status = spotifyOAuthService.status(userId);
        assertThat(status.connected()).isTrue();
        assertThat(status.spotifyUserId()).isEqualTo("spotify-" + userId);
    }
}
