import { api } from '../../api/http';
import type {
  Album,
  Artist,
  CatalogSearchType,
  PageResponse,
  SearchItem,
  Track,
} from '../../api/types';

export interface CatalogSearchInput {
  query: string;
  type: CatalogSearchType;
  page: number;
  size?: number;
}

export const catalogApi = {
  search({ query, type, page, size = 20 }: CatalogSearchInput): Promise<PageResponse<SearchItem>> {
    const params = new URLSearchParams({ q: query, type, page: String(page), size: String(size) });
    return api.get<PageResponse<SearchItem>>(`/catalog/search?${params.toString()}`);
  },
  artist(mbid: string): Promise<Artist> {
    return api.get<Artist>(`/catalog/artists/${encodeURIComponent(mbid)}`);
  },
  artistAlbums(mbid: string): Promise<Album[]> {
    return api.get<Album[]>(`/catalog/artists/${encodeURIComponent(mbid)}/albums`);
  },
  album(mbid: string): Promise<Album> {
    return api.get<Album>(`/catalog/albums/${encodeURIComponent(mbid)}`);
  },
  track(mbid: string): Promise<Track> {
    return api.get<Track>(`/catalog/tracks/${encodeURIComponent(mbid)}`);
  },
};

export const catalogKeys = {
  search: (input: CatalogSearchInput) => ['catalog', 'search', input] as const,
  artist: (mbid: string) => ['catalog', 'artist', mbid] as const,
  artistAlbums: (mbid: string) => ['catalog', 'artist', mbid, 'albums'] as const,
  album: (mbid: string) => ['catalog', 'album', mbid] as const,
  track: (mbid: string) => ['catalog', 'track', mbid] as const,
};
