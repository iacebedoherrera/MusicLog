import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import type { ReviewTargetType } from '../api/types';
import { EmptyState, ErrorState, LoadingState } from '../components/AsyncState';
import { Pagination } from '../components/Pagination';
import { catalogApi, catalogKeys } from '../features/catalog/api';
import { reviewKeys, reviewsApi } from '../features/reviews/api';
import { ReviewCard } from '../features/reviews/ReviewCard';
import { useAuth } from '../features/auth/useAuth';

function NewReviewLink({
  targetMbid,
  targetType,
}: {
  targetMbid: string;
  targetType: ReviewTargetType;
}) {
  const { user } = useAuth();
  if (!user) {
    return (
      <Link
        className="rounded-lg bg-ink px-4 py-2.5 text-sm font-semibold text-white hover:bg-moss"
        to="/login"
      >
        Entra para reseñar
      </Link>
    );
  }
  const params = new URLSearchParams({ targetMbid, targetType });
  return (
    <Link
      className="rounded-lg bg-ink px-4 py-2.5 text-sm font-semibold text-white hover:bg-moss"
      to={`/reviews/new?${params.toString()}`}
    >
      Escribir reseña
    </Link>
  );
}

function AlbumCover({ src, title }: { src: string | null; title: string }) {
  if (!src) {
    return (
      <div className="aspect-square w-full rounded-xl bg-moss p-5 text-sm font-bold uppercase tracking-widest text-paper">
        Sin portada
      </div>
    );
  }
  return (
    <img
      className="aspect-square w-full rounded-xl object-cover shadow-lg"
      src={src}
      alt={`Portada de ${title}`}
    />
  );
}

function ReviewsSection({ mbid, type }: { mbid: string; type: 'album' | 'artist' }) {
  const [page, setPage] = useState(0);
  const reviews = useQuery({
    queryKey: type === 'album' ? reviewKeys.album(mbid, page) : reviewKeys.artist(mbid, page),
    queryFn: () =>
      type === 'album' ? reviewsApi.albumReviews(mbid, page) : reviewsApi.artistReviews(mbid, page),
  });

  return (
    <section className="mt-12 border-t border-stone-200 pt-10" aria-labelledby="reviews-heading">
      <h2 id="reviews-heading" className="font-display text-4xl">
        Reseñas
      </h2>
      {reviews.isPending ? <LoadingState label="Cargando reseñas…" /> : null}
      {reviews.isError ? (
        <ErrorState error={reviews.error} retry={() => void reviews.refetch()} />
      ) : null}
      {reviews.data ? (
        <>
          {reviews.data.items.length === 0 ? (
            <div className="mt-5">
              <EmptyState title="Aún no hay reseñas.">
                Sé la primera persona en dejar una nota.
              </EmptyState>
            </div>
          ) : (
            <div className="mt-5 grid gap-4">
              {reviews.data.items.map((review) => (
                <ReviewCard key={review.id} review={review} />
              ))}
            </div>
          )}
          <Pagination
            page={reviews.data.page}
            hasNext={reviews.data.hasNext}
            onPageChange={setPage}
          />
        </>
      ) : null}
    </section>
  );
}

export function ArtistDetailPage() {
  const mbid = useParams().mbid ?? '';
  const artist = useQuery({
    queryKey: catalogKeys.artist(mbid),
    queryFn: () => catalogApi.artist(mbid),
    enabled: Boolean(mbid),
  });
  const albums = useQuery({
    queryKey: catalogKeys.artistAlbums(mbid),
    queryFn: () => catalogApi.artistAlbums(mbid),
    enabled: Boolean(mbid),
  });

  if (!mbid) return <ErrorState error={new Error('El artista no es válido.')} />;
  if (artist.isPending) return <LoadingState label="Cargando artista…" />;
  if (artist.isError)
    return <ErrorState error={artist.error} retry={() => void artist.refetch()} />;
  if (!artist.data) return null;

  return (
    <div>
      <Link className="text-sm font-semibold text-stone-600 underline hover:text-ink" to="/">
        ← Volver a descubrir
      </Link>
      <section className="mt-6 rounded-3xl bg-moss p-7 text-paper sm:p-12">
        <p className="text-sm font-bold uppercase tracking-[0.2em] text-paper/70">Artista</p>
        <h1 className="mt-3 font-display text-5xl sm:text-7xl">{artist.data.name}</h1>
        {artist.data.disambiguation ? (
          <p className="mt-5 max-w-2xl text-lg text-paper/80">{artist.data.disambiguation}</p>
        ) : null}
        <div className="mt-7">
          <NewReviewLink targetMbid={artist.data.mbid} targetType="ARTIST" />
        </div>
      </section>

      <section className="mt-12" aria-labelledby="albums-heading">
        <h2 id="albums-heading" className="font-display text-4xl">
          Discografía
        </h2>
        {albums.isPending ? <LoadingState label="Cargando discografía…" /> : null}
        {albums.isError ? (
          <ErrorState error={albums.error} retry={() => void albums.refetch()} />
        ) : null}
        {albums.data ? (
          albums.data.length === 0 ? (
            <EmptyState title="No hay álbumes disponibles." />
          ) : (
            <ul className="mt-5 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
              {albums.data.map((album) => (
                <li key={album.mbid}>
                  <Link className="block group" to={`/albums/${album.mbid}`}>
                    <AlbumCover src={album.coverArtUrl} title={album.title} />
                    <p className="mt-3 font-semibold group-hover:underline">{album.title}</p>
                    <p className="text-sm text-stone-600">
                      {album.releaseDate ?? 'Fecha desconocida'}
                    </p>
                  </Link>
                </li>
              ))}
            </ul>
          )
        ) : null}
      </section>
      <ReviewsSection mbid={artist.data.mbid} type="artist" />
    </div>
  );
}

export function AlbumDetailPage() {
  const mbid = useParams().mbid ?? '';
  const album = useQuery({
    queryKey: catalogKeys.album(mbid),
    queryFn: () => catalogApi.album(mbid),
    enabled: Boolean(mbid),
  });

  if (!mbid) return <ErrorState error={new Error('El álbum no es válido.')} />;
  if (album.isPending) return <LoadingState label="Cargando álbum…" />;
  if (album.isError) return <ErrorState error={album.error} retry={() => void album.refetch()} />;
  if (!album.data) return null;

  return (
    <div>
      <Link className="text-sm font-semibold text-stone-600 underline hover:text-ink" to="/">
        ← Volver a descubrir
      </Link>
      <section className="mt-6 grid gap-8 sm:grid-cols-[minmax(0,360px)_1fr] sm:items-end">
        <AlbumCover src={album.data.coverArtUrl} title={album.data.title} />
        <div>
          <p className="text-sm font-bold uppercase tracking-[0.2em] text-signal">
            {album.data.type ?? 'Álbum'}
          </p>
          <h1 className="mt-3 font-display text-5xl leading-none sm:text-7xl">
            {album.data.title}
          </h1>
          {album.data.artistMbid ? (
            <Link
              className="mt-5 inline-block text-lg font-semibold underline"
              to={`/artists/${album.data.artistMbid}`}
            >
              Ver artista
            </Link>
          ) : null}
          <p className="mt-2 text-stone-600">
            {album.data.releaseDate ?? 'Fecha de publicación desconocida'}
          </p>
          <div className="mt-7">
            <NewReviewLink targetMbid={album.data.mbid} targetType="ALBUM" />
          </div>
        </div>
      </section>
      <ReviewsSection mbid={album.data.mbid} type="album" />
    </div>
  );
}

export function TrackDetailPage() {
  const mbid = useParams().mbid ?? '';
  const track = useQuery({
    queryKey: catalogKeys.track(mbid),
    queryFn: () => catalogApi.track(mbid),
    enabled: Boolean(mbid),
  });

  if (!mbid) return <ErrorState error={new Error('La pista no es válida.')} />;
  if (track.isPending) return <LoadingState label="Cargando pista…" />;
  if (track.isError) return <ErrorState error={track.error} retry={() => void track.refetch()} />;
  if (!track.data) return null;

  const duration = track.data.durationMs
    ? `${Math.floor(track.data.durationMs / 60000)}:${String(Math.floor((track.data.durationMs % 60000) / 1000)).padStart(2, '0')}`
    : null;
  return (
    <div>
      <Link className="text-sm font-semibold text-stone-600 underline hover:text-ink" to="/">
        ← Volver a descubrir
      </Link>
      <section className="mt-6 rounded-3xl border border-stone-200 bg-white p-7 sm:p-12">
        <p className="text-sm font-bold uppercase tracking-[0.2em] text-signal">Pista</p>
        <h1 className="mt-3 font-display text-5xl sm:text-7xl">{track.data.title}</h1>
        <dl className="mt-8 grid gap-4 text-sm sm:grid-cols-3">
          {track.data.trackNumber ? (
            <div>
              <dt className="text-stone-500">Número</dt>
              <dd className="mt-1 font-semibold">{track.data.trackNumber}</dd>
            </div>
          ) : null}
          {duration ? (
            <div>
              <dt className="text-stone-500">Duración</dt>
              <dd className="mt-1 font-semibold">{duration}</dd>
            </div>
          ) : null}
          {track.data.albumMbid ? (
            <div>
              <dt className="text-stone-500">Álbum</dt>
              <dd className="mt-1">
                <Link className="font-semibold underline" to={`/albums/${track.data.albumMbid}`}>
                  Ver álbum
                </Link>
              </dd>
            </div>
          ) : null}
        </dl>
        <div className="mt-8">
          <NewReviewLink targetMbid={track.data.mbid} targetType="TRACK" />
        </div>
      </section>
      <p className="mt-8 rounded-xl bg-stone-100 p-4 text-sm text-stone-600">
        El backend todavía no expone una colección pública de reseñas de pistas. Puedes crear una
        reseña y abrir su detalle individual.
      </p>
    </div>
  );
}
