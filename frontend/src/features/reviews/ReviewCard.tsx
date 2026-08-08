import { Link } from 'react-router-dom';
import type { Review } from '../../api/types';

function formattedDate(value: string): string {
  return new Intl.DateTimeFormat('es-ES', { dateStyle: 'medium' }).format(new Date(value));
}

export function ReviewCard({ review }: { review: Review }) {
  return (
    <article className="rounded-xl border border-stone-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <Link className="font-semibold hover:underline" to={`/reviews/${review.id}`}>
          {review.author.displayName}
          <span className="ml-1 font-normal text-stone-500">@{review.author.username}</span>
        </Link>
        {review.rating !== null ? (
          <span
            className="rounded-full bg-signal px-3 py-1 text-sm font-bold text-white"
            aria-label={`Puntuación ${review.rating} de 10`}
          >
            {review.rating}/10
          </span>
        ) : null}
      </div>
      {review.reviewText ? (
        <p className="mt-4 whitespace-pre-wrap text-stone-700">{review.reviewText}</p>
      ) : null}
      {review.containsSpoilers ? (
        <p className="mt-4 text-xs font-semibold uppercase tracking-wide text-signal">
          Contiene spoilers
        </p>
      ) : null}
      <p className="mt-4 text-xs text-stone-500">{formattedDate(review.createdAt)}</p>
    </article>
  );
}
