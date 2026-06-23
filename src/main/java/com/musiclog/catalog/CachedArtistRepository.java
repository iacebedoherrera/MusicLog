package com.musiclog.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CachedArtistRepository extends JpaRepository<CachedArtist, String> {
}
