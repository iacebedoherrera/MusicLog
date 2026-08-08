package com.musiclog.catalog;

import com.musiclog.catalog.dto.AlbumResponse;
import com.musiclog.catalog.dto.ArtistResponse;
import com.musiclog.catalog.dto.TrackResponse;
import com.musiclog.catalog.dto.SearchResponse;
import com.musiclog.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catálogo", description = "Consulta de artistas, álbumes y pistas de MusicBrainz. Las respuestas se almacenan en caché.")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar en el catálogo", description = "El parámetro type acepta: artist, album o track.")
    public PageResponse<SearchResponse.SearchItem> search(
            @Parameter(description = "Texto a buscar", example = "Radiohead") @RequestParam String q,
            @Parameter(description = "Tipo de resultado", example = "artist") @RequestParam String type,
            @Parameter(description = "Página, empezando en 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Resultados por página, entre 1 y 50") @RequestParam(defaultValue = "20") int size) {
        return catalogService.search(q, type, page, size);
    }

    @GetMapping("/artists/{mbid}")
    @Operation(summary = "Obtener un artista por MBID")
    public ArtistResponse artist(@PathVariable String mbid) {
        return catalogService.artist(mbid);
    }

    @GetMapping("/albums/{mbid}")
    @Operation(summary = "Obtener un álbum por MBID")
    public AlbumResponse album(@PathVariable String mbid) {
        return catalogService.album(mbid);
    }

    @GetMapping("/tracks/{mbid}")
    @Operation(summary = "Obtener una pista por MBID")
    public TrackResponse track(@PathVariable String mbid) {
        return catalogService.track(mbid);
    }

    @GetMapping("/artists/{mbid}/albums")
    @Operation(summary = "Listar álbumes de un artista")
    public List<AlbumResponse> artistAlbums(@PathVariable String mbid) {
        return catalogService.artistAlbums(mbid);
    }
}
