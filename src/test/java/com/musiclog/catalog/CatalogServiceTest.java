package com.musiclog.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musiclog.catalog.dto.ArtistResponse;
import com.musiclog.shared.config.MusicBrainzProperties;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class CatalogServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void artistUsesPersistentCacheBeforeCallingMusicBrainz() {
        CachedArtistRepository artistRepository = mock(CachedArtistRepository.class);
        CachedAlbumRepository albumRepository = mock(CachedAlbumRepository.class);
        CachedTrackRepository trackRepository = mock(CachedTrackRepository.class);
        MusicBrainzClient musicBrainzClient = mock(MusicBrainzClient.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("catalog:artist:mbid-1")).thenReturn(null);
        when(artistRepository.findById("mbid-1")).thenReturn(Optional.of(new CachedArtist("mbid-1", "Radiohead", "Radiohead", "GB", null)));
        CatalogService service = new CatalogService(
                artistRepository,
                albumRepository,
                trackRepository,
                musicBrainzClient,
                redisTemplate,
                new ObjectMapper(),
                new MusicBrainzProperties("https://musicbrainz.org/ws/2", "https://coverartarchive.org", "MusicLog/0.1 (contacto@example.com)"));

        ArtistResponse response = service.artist("mbid-1");

        assertThat(response.name()).isEqualTo("Radiohead");
        verify(musicBrainzClient, never()).artist("mbid-1");
    }
}
