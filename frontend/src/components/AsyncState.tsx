import type { ReactNode } from 'react';
import { ApiClientError } from '../api/http';

export function LoadingState({ label = 'Cargando…' }: { label?: string }) {
  return (
    <div className="flex min-h-48 items-center justify-center" role="status" aria-live="polite">
      <span className="h-6 w-6 animate-spin rounded-full border-2 border-ink border-t-signal" />
      <span className="ml-3 text-sm text-stone-600">{label}</span>
    </div>
  );
}

export function EmptyState({ title, children }: { title: string; children?: ReactNode }) {
  return (
    <section className="rounded-2xl border border-dashed border-stone-300 bg-white p-8 text-center">
      <h2 className="font-display text-2xl text-ink">{title}</h2>
      {children ? <div className="mt-2 text-sm text-stone-600">{children}</div> : null}
    </section>
  );
}

export function ErrorState({ error, retry }: { error: unknown; retry?: () => void }) {
  const message =
    error instanceof ApiClientError ? error.message : 'Ha ocurrido un error inesperado.';

  return (
    <section className="rounded-2xl border border-red-200 bg-red-50 p-5" role="alert">
      <h2 className="font-semibold text-red-950">No se pudo cargar este contenido</h2>
      <p className="mt-1 text-sm text-red-800">{message}</p>
      {retry ? (
        <button
          className="mt-4 rounded-lg bg-red-900 px-3 py-2 text-sm font-semibold text-white"
          onClick={retry}
        >
          Reintentar
        </button>
      ) : null}
    </section>
  );
}

export function FormError({ error }: { error: unknown }) {
  if (!error) return null;
  const message =
    error instanceof ApiClientError
      ? error.message
      : 'No se ha podido guardar. Inténtalo de nuevo.';
  return (
    <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">
      {message}
    </p>
  );
}
