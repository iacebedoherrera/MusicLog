import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../features/auth/useAuth';

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-semibold transition ${
    isActive ? 'bg-white text-ink shadow-sm' : 'text-stone-600 hover:text-ink'
  }`;

export function AppLayout() {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const onLogout = () => {
    logout();
    void navigate('/');
  };

  return (
    <div className="min-h-screen bg-paper text-ink">
      <header className="border-b border-stone-200 bg-paper/95 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4 sm:px-6">
          <Link className="font-display text-3xl tracking-tight" to="/">
            MusicLog<span className="text-signal">.</span>
          </Link>
          <nav className="flex items-center gap-1" aria-label="Navegación principal">
            <NavLink className={navLinkClass} to="/">
              Descubrir
            </NavLink>
            {user ? (
              <>
                <span className="hidden px-2 text-sm text-stone-500 sm:inline">
                  Hola, {user.displayName}
                </span>
                <button
                  className="rounded-md px-3 py-2 text-sm font-semibold text-stone-600 hover:text-ink"
                  onClick={onLogout}
                >
                  Salir
                </button>
              </>
            ) : (
              <NavLink className={navLinkClass} to="/login">
                Entrar
              </NavLink>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6 sm:py-12">
        <Outlet />
      </main>
    </div>
  );
}
