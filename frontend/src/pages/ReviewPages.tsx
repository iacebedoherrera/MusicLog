import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, Navigate, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { ApiClientError } from '../api/http';
import type { ReviewTargetType } from '../api/types';
import { EmptyState, ErrorState, FormError, LoadingState } from '../components/AsyncState';
import { useAuth } from '../features/auth/useAuth';
import { reviewKeys, reviewsApi } from '../features/reviews/api';

const inputClass =
  'mt-1 block w-full rounded-lg border-stone-300 bg-white px-3 py-2.5 text-ink shadow-sm outline-none focus:border-signal focus:ring-signal';

function validTargetType(value: string | null): value is ReviewTargetType {
  return value === 'ALBUM' || value === 'ARTIST' || value === 'TRACK';
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('es-ES', { dateStyle: 'long' }).format(new Date(value));
}

export function ReviewDetailPage() {
  const id = useParams().id ?? '';
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const review = useQuery({
    queryKey: reviewKeys.detail(id),
    queryFn: () => reviewsApi.get(id),
    enabled: Boolean(id),
  });
  const remove = useMutation({
    mutationFn: () => reviewsApi.remove(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['reviews'] });
      await navigate('/');
    },
  });

  if (!id) return <ErrorState error={new Error('La reseña no es válida.')} />;
  if (review.isPending) return <LoadingState label="Cargando reseña…" />;
  if (review.isError)
    return <ErrorState error={review.error} retry={() => void review.refetch()} />;
  if (!review.data) return null;

  const isOwner = user?.id === review.data.author.id;
  const onDelete = () => {
    if (window.confirm('¿Quieres eliminar esta reseña? Esta acción no se puede deshacer.')) {
      remove.mutate();
    }
  };

  return (
    <article className="mx-auto max-w-3xl rounded-2xl border border-stone-200 bg-white p-6 shadow-sm sm:p-10">
      <Link className="text-sm font-semibold text-stone-600 underline hover:text-ink" to="/">
        ← Volver a descubrir
      </Link>
      <div className="mt-8 flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold uppercase tracking-[0.2em] text-signal">
            Reseña de {review.data.targetType.toLowerCase()}
          </p>
          <h1 className="mt-2 font-display text-4xl">{review.data.author.displayName}</h1>
          <p className="mt-1 text-stone-600">
            @{review.data.author.username} · {formatDate(review.data.createdAt)}
          </p>
        </div>
        {review.data.rating !== null ? (
          <span className="rounded-full bg-signal px-4 py-2 text-lg font-bold text-white">
            {review.data.rating}/10
          </span>
        ) : null}
      </div>

      {review.data.reviewText ? (
        review.data.containsSpoilers ? (
          <details className="mt-8 rounded-xl bg-stone-100 p-5">
            <summary className="cursor-pointer font-semibold">
              Esta reseña contiene spoilers. Mostrar texto.
            </summary>
            <p className="mt-4 whitespace-pre-wrap leading-7 text-stone-700">
              {review.data.reviewText}
            </p>
          </details>
        ) : (
          <p className="mt-8 whitespace-pre-wrap text-lg leading-8 text-stone-700">
            {review.data.reviewText}
          </p>
        )
      ) : (
        <EmptyState title="Una puntuación sin texto." />
      )}

      {isOwner ? (
        <div className="mt-10 flex flex-wrap gap-3 border-t border-stone-200 pt-6">
          <Link
            className="rounded-lg bg-ink px-4 py-2.5 text-sm font-semibold text-white hover:bg-moss"
            to={`/reviews/${review.data.id}/edit`}
          >
            Editar reseña
          </Link>
          <button
            className="rounded-lg border border-red-300 px-4 py-2.5 text-sm font-semibold text-red-800 hover:bg-red-50 disabled:opacity-50"
            onClick={onDelete}
            disabled={remove.isPending}
          >
            {remove.isPending ? 'Eliminando…' : 'Eliminar'}
          </button>
          <FormError error={remove.error} />
        </div>
      ) : null}
    </article>
  );
}

export function ReviewFormPage() {
  const id = useParams().id;
  const isEdit = Boolean(id);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const existingReview = useQuery({
    queryKey: reviewKeys.detail(id ?? ''),
    queryFn: () => reviewsApi.get(id ?? ''),
    enabled: isEdit,
  });
  const [rating, setRating] = useState<number | null>(null);
  const [reviewText, setReviewText] = useState('');
  const [containsSpoilers, setContainsSpoilers] = useState(false);
  const [validationError, setValidationError] = useState<string | null>(null);

  useEffect(() => {
    if (existingReview.data) {
      setRating(existingReview.data.rating);
      setReviewText(existingReview.data.reviewText ?? '');
      setContainsSpoilers(existingReview.data.containsSpoilers);
    }
  }, [existingReview.data]);

  const targetMbid = existingReview.data?.targetMbid ?? searchParams.get('targetMbid') ?? '';
  const targetTypeValue = existingReview.data?.targetType ?? searchParams.get('targetType');
  const targetType = validTargetType(targetTypeValue) ? targetTypeValue : null;

  const save = useMutation({
    mutationFn: async () => {
      const normalizedText = reviewText.trim() || null;
      if (rating === null && normalizedText === null) {
        throw new ApiClientError(400, 'Escribe una reseña, añade una puntuación o ambas cosas.');
      }
      if (isEdit && id) {
        return reviewsApi.update(id, { rating, reviewText: normalizedText, containsSpoilers });
      }
      if (!targetType || !targetMbid) {
        throw new ApiClientError(400, 'Falta el álbum, artista o pista que quieres reseñar.');
      }
      return reviewsApi.create({
        targetMbid,
        targetType,
        rating,
        reviewText: normalizedText,
        containsSpoilers,
      });
    },
    onSuccess: async (savedReview) => {
      await queryClient.invalidateQueries({ queryKey: ['reviews'] });
      await navigate(`/reviews/${savedReview.id}`);
    },
  });

  if (isEdit && existingReview.isPending) return <LoadingState label="Cargando reseña…" />;
  if (isEdit && existingReview.isError)
    return <ErrorState error={existingReview.error} retry={() => void existingReview.refetch()} />;
  if (isEdit && existingReview.data && user?.id !== existingReview.data.author.id) {
    return <Navigate to={`/reviews/${existingReview.data.id}`} replace />;
  }
  if (!isEdit && (!targetMbid || !targetType)) {
    return (
      <ErrorState
        error={new ApiClientError(400, 'No se ha indicado un elemento del catálogo para reseñar.')}
      />
    );
  }

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setValidationError(null);
    save.mutate();
  };

  return (
    <section className="mx-auto max-w-2xl">
      <Link className="text-sm font-semibold text-stone-600 underline hover:text-ink" to="/">
        ← Cancelar y volver
      </Link>
      <p className="mt-8 text-sm font-bold uppercase tracking-[0.2em] text-signal">
        {isEdit ? 'Editar reseña' : 'Nueva reseña'}
      </p>
      <h1 className="mt-2 font-display text-5xl">
        {isEdit ? 'Cambia de opinión.' : 'Deja constancia.'}
      </h1>
      <p className="mt-3 text-stone-600">
        {targetType?.toLowerCase()}: <span className="font-mono text-xs">{targetMbid}</span>
      </p>
      <form
        className="mt-8 space-y-6 rounded-2xl border border-stone-200 bg-white p-6 shadow-sm"
        onSubmit={onSubmit}
      >
        <FormError error={validationError ?? save.error} />
        <label className="block text-sm font-semibold" htmlFor="rating">
          Puntuación (opcional)
          <input
            className={inputClass}
            id="rating"
            type="number"
            min="1"
            max="10"
            value={rating ?? ''}
            onChange={(event) =>
              setRating(event.target.value === '' ? null : Number(event.target.value))
            }
            aria-describedby="rating-help"
          />
          <span className="mt-1 block text-xs font-normal text-stone-500" id="rating-help">
            Entre 1 y 10.
          </span>
        </label>
        <label className="block text-sm font-semibold" htmlFor="review-text">
          Tu reseña (opcional)
          <textarea
            className={inputClass}
            id="review-text"
            rows={9}
            value={reviewText}
            onChange={(event) => setReviewText(event.target.value)}
            maxLength={10000}
          />
        </label>
        <label className="flex items-start gap-3 text-sm font-semibold">
          <input
            className="mt-0.5 rounded border-stone-300 text-signal focus:ring-signal"
            type="checkbox"
            checked={containsSpoilers}
            onChange={(event) => setContainsSpoilers(event.target.checked)}
          />
          <span>Esta reseña contiene spoilers.</span>
        </label>
        <button
          className="rounded-lg bg-ink px-5 py-3 font-semibold text-white hover:bg-moss disabled:opacity-50"
          disabled={save.isPending}
        >
          {save.isPending ? 'Guardando…' : isEdit ? 'Guardar cambios' : 'Publicar reseña'}
        </button>
      </form>
    </section>
  );
}
