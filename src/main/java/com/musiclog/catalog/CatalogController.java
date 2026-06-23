package com.musiclog.catalog;

import com.musiclog.catalog.dto.AlbumResponse;
import com.musiclog.catalog.dto.ArtistResponse;
import com.musiclog.catalog.dto.SearchResponse;
import com.musiclog.catalog.dto.TrackResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam String q, @RequestParam String type) {
        return catalogService.search(q, type);
    }

    @GetMapping("/artists/{mbid}")
    public ArtistResponse artist(@PathVariable String mbid) {
        return catalogService.artist(mbid);
    }

    @GetMapping("/albums/{mbid}")
    public AlbumResponse album(@PathVariable String mbid) {
        return catalogService.album(mbid);
    }

    @GetMapping("/tracks/{mbid}")
    public TrackResponse track(@PathVariable String mbid) {
        return catalogService.track(mbid);
    }

    @GetMapping("/artists/{mbid}/albums")
    public List<AlbumResponse> artistAlbums(@PathVariable String mbid) {
        return catalogService.artistAlbums(mbid);
    }
}
