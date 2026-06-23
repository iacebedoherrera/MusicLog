package com.musiclog.spotifyconnector;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotifyConnectionRepository extends JpaRepository<SpotifyConnection, UUID> {

    Optional<SpotifyConnection> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
