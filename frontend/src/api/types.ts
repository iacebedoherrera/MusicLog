export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: Record<string, string>;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface UserProfile {
  id: string;
  username: string;
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
  createdAt: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  username: string;
}

export type CatalogSearchType = 'artist' | 'album' | 'track';

export interface SearchItem {
  mbid: string;
  title: string;
  subtitle: string | null;
  score: number | null;
}

export interface Artist {
  mbid: string;
  name: string;
  sortName: string | null;
  country: string | null;
  disambiguation: string | null;
}

export interface Album {
  mbid: string;
  title: string;
  artistMbid: string | null;
  releaseDate: string | null;
  type: string | null;
  coverArtUrl: string | null;
}

export interface Track {
  mbid: string;
  title: string;
  albumMbid: string | null;
  artistMbid: string | null;
  durationMs: number | null;
  trackNumber: number | null;
}

export type ReviewTargetType = 'ALBUM' | 'ARTIST' | 'TRACK';

export interface ReviewAuthor {
  id: string;
  username: string;
  displayName: string;
  avatarUrl: string | null;
}

export interface Review {
  id: string;
  userId: string;
  author: ReviewAuthor;
  targetMbid: string;
  targetType: ReviewTargetType;
  rating: number | null;
  reviewText: string | null;
  containsSpoilers: boolean;
  createdAt: string;
  updatedAt: string;
}
