package com.musiclog.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.musiclog.SpringModulithIntegrationTest;
import com.musiclog.catalog.dto.ArtistResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringModulithIntegrationTest
class CatalogModuleIntegrationTest {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CachedArtistRepository artistRepository;

    @MockBean
    private MusicBrainzClient musicBrainzClient;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void artistLookupStoresPersistentCacheEntry() {
        String mbid = "artist-mbid-1";
        when(valueOperations.get("catalog:artist:" + mbid)).thenReturn(null);
        when(musicBrainzClient.artist(mbid)).thenReturn(Map.of(
                "id", mbid,
                "name", "Radiohead",
                "sort-name", "Radiohead",
                "country", "GB"));

        ArtistResponse response = catalogService.artist(mbid);

        assertThat(response.name()).isEqualTo("Radiohead");
        assertThat(artistRepository.findById(mbid)).isPresent();
    }
}
