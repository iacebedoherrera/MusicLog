import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <section className="mx-auto max-w-xl py-16 text-center">
      <p className="text-sm font-bold uppercase tracking-[0.2em] text-signal">404</p>
      <h1 className="mt-3 font-display text-5xl">Esta cara B no existe.</h1>
      <p className="mt-4 text-stone-600">La página que buscas no está en el catálogo.</p>
      <Link
        className="mt-8 inline-block rounded-lg bg-ink px-5 py-3 font-semibold text-white hover:bg-moss"
        to="/"
      >
        Volver a descubrir
      </Link>
    </section>
  );
}
