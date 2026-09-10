import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import type { CatalogSearchType, SearchItem } from '../api/types';
import { EmptyState, ErrorState, LoadingState } from '../components/AsyncState';
import { Pagination } from '../components/Pagination';
import { catalogApi, catalogKeys } from '../features/catalog/api';

const searchTypes: Array<{ value: CatalogSearchType; label: string }> = [
  { value: 'artist', label: 'Artistas' },
  { value: 'album', label: 'Álbumes' },
  { value: 'track', label: 'Pistas' },
];

function validSearchType(value: string | null): value is CatalogSearchType {
  return value === 'artist' || value === 'album' || value === 'track';
}

function pathForResult(type: CatalogSearchType, item: SearchItem): string {
  const routes: Record<CatalogSearchType, string> = {
    artist: 'artists',
    album: 'albums',
    track: 'tracks',
  };
  return `/${routes[type]}/${item.mbid}`;
}

export function CatalogSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.get('q')?.trim() ?? '';
  const typeParam = searchParams.get('type');
  const type: CatalogSearchType = validSearchType(typeParam) ? typeParam : 'album';
  const page = Math.max(Number(searchParams.get('page') ?? '0') || 0, 0);
  const [input, setInput] = useState(query);
  const [selectedType, setSelectedType] = useState<CatalogSearchType>(type);

  useEffect(() => {
    setInput(query);
    setSelectedType(type);
  }, [query, type]);

  const search = useQuery({
    queryKey: catalogKeys.search({ query, type, page }),
    queryFn: () => catalogApi.search({ query, type, page }),
    enabled: query.length >= 2,
  });

  const submit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextQuery = input.trim();
    if (nextQuery.length >= 2) {
      setSearchParams({ q: nextQuery, type: selectedType, page: '0' });
    }
  };

  const changePage = (nextPage: number) => {
    setSearchParams({ q: query, type, page: String(nextPage) });
  };

  return (
    <div>
      <section className="max-w-3xl py-8 sm:py-16">
        <p className="text-sm font-bold uppercase tracking-[0.2em] text-signal">
          Tu archivo musical
        </p>
        <h1 className="mt-3 font-display text-5xl leading-none sm:text-7xl">
          Lo que escuchas merece quedarse.
        </h1>
        <p className="mt-5 max-w-xl text-lg leading-8 text-stone-600">
          Busca discos, artistas y canciones para descubrirlos, puntuarlos y recordarlos.
        </p>
        <form className="mt-8 flex flex-col gap-3 sm:flex-row" onSubmit={submit}>
          <label className="sr-only" htmlFor="catalog-search">
            Buscar en el catálogo
          </label>
          <input
            className="min-w-0 flex-1 rounded-xl border-stone-300 bg-white px-4 py-3 shadow-sm outline-none focus:border-signal focus:ring-signal"
            id="catalog-search"
            value={input}
            onChange={(event) => setInput(event.target.value)}
            placeholder="Ej. The Cure, Dummy, Teardrop"
            minLength={2}
            required
          />
          <div className="relative w-full sm:w-36">
            <select
              className="w-full appearance-none rounded-xl border-stone-300 bg-none bg-white px-4 py-3 pr-10 font-semibold outline-none focus:border-signal focus:ring-signal"
              value={selectedType}
              onChange={(event) => setSelectedType(event.target.value as CatalogSearchType)}
              aria-label="Tipo de búsqueda"
            >
              {searchTypes.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <svg
              aria-hidden="true"
              className="pointer-events-none absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-stone-500"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="m6 9 6 6 6-6" />
            </svg>
          </div>
          <button className="rounded-xl bg-ink px-6 py-3 font-semibold text-white hover:bg-moss">
            Buscar
          </button>
        </form>
      </section>

      {query.length === 0 ? (
        <EmptyState title="Empieza con algo que te obsesione.">
          Escribe al menos dos caracteres para buscar en MusicBrainz.
        </EmptyState>
      ) : null}
      {query.length === 1 ? (
        <EmptyState title="Necesitamos un poco más.">
          La búsqueda requiere al menos dos caracteres.
        </EmptyState>
      ) : null}
      {query.length >= 2 && search.isPending ? (
        <LoadingState label="Buscando en el catálogo…" />
      ) : null}
      {search.isError ? (
        <ErrorState error={search.error} retry={() => void search.refetch()} />
      ) : null}
      {search.data ? (
        <section aria-labelledby="search-results-heading">
          <div className="mb-4 flex items-baseline justify-between">
            <h2 id="search-results-heading" className="font-display text-3xl">
              Resultados para “{query}”
            </h2>
            <span className="text-sm text-stone-500">{search.data.totalElements} encontrados</span>
          </div>
          {search.data.items.length === 0 ? (
            <EmptyState title="No hemos encontrado nada.">
              Prueba con otro término o tipo de búsqueda.
            </EmptyState>
          ) : (
            <>
              <ul className="grid gap-3" aria-live="polite">
                {search.data.items.map((item) => (
                  <li key={item.mbid}>
                    <Link
                      className="block rounded-xl border border-stone-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-stone-400"
                      to={pathForResult(type, item)}
                    >
                      <p className="font-semibold text-ink">{item.title}</p>
                      {item.subtitle ? (
                        <p className="mt-1 text-sm text-stone-600">{item.subtitle}</p>
                      ) : null}
                    </Link>
                  </li>
                ))}
              </ul>
              <Pagination
                page={search.data.page}
                hasNext={search.data.hasNext}
                onPageChange={changePage}
              />
            </>
          )}
        </section>
      ) : null}
    </div>
  );
}
