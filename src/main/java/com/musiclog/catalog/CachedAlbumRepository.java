package com.musiclog.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CachedAlbumRepository extends JpaRepository<CachedAlbum, String> {

    List<CachedAlbum> findByArtistMbidOrderByReleaseDateDesc(String artistMbid);
}
