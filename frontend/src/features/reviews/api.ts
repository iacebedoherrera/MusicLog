import { api } from '../../api/http';
import type { PageResponse, Review, ReviewTargetType } from '../../api/types';

export interface ReviewInput {
  targetMbid: string;
  targetType: ReviewTargetType;
  rating: number | null;
  reviewText: string | null;
  containsSpoilers: boolean;
}

export interface ReviewUpdateInput {
  rating: number | null;
  reviewText: string | null;
  containsSpoilers: boolean;
}

export const reviewsApi = {
  create(input: ReviewInput): Promise<Review> {
    return api.post<Review>('/reviews', input);
  },
  get(id: string): Promise<Review> {
    return api.get<Review>(`/reviews/${encodeURIComponent(id)}`);
  },
  update(id: string, input: ReviewUpdateInput): Promise<Review> {
    return api.put<Review>(`/reviews/${encodeURIComponent(id)}`, input);
  },
  remove(id: string): Promise<void> {
    return api.delete(`/reviews/${encodeURIComponent(id)}`);
  },
  albumReviews(mbid: string, page: number, size = 20): Promise<PageResponse<Review>> {
    return api.get<PageResponse<Review>>(
      `/catalog/albums/${encodeURIComponent(mbid)}/reviews?page=${page}&size=${size}`,
    );
  },
  artistReviews(mbid: string, page: number, size = 20): Promise<PageResponse<Review>> {
    return api.get<PageResponse<Review>>(
      `/catalog/artists/${encodeURIComponent(mbid)}/reviews?page=${page}&size=${size}`,
    );
  },
};

export const reviewKeys = {
  detail: (id: string) => ['reviews', id] as const,
  album: (mbid: string, page: number) => ['reviews', 'album', mbid, page] as const,
  artist: (mbid: string, page: number) => ['reviews', 'artist', mbid, page] as const,
};
