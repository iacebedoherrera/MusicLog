package com.musiclog.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CachedTrackRepository extends JpaRepository<CachedTrack, String> {
}
