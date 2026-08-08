import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { FormError } from '../components/AsyncState';
import { useAuth } from '../features/auth/useAuth';

const inputClass =
  'mt-1 block w-full rounded-lg border-stone-300 bg-white px-3 py-2.5 text-ink shadow-sm outline-none focus:border-signal focus:ring-signal';

export function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<unknown>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await login({ usernameOrEmail, password });
      await navigate('/');
    } catch (submissionError) {
      setError(submissionError);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="mx-auto max-w-md">
      <p className="text-sm font-bold uppercase tracking-[0.2em] text-signal">
        Bienvenido de nuevo
      </p>
      <h1 className="mt-2 font-display text-5xl">Entra a tu diario musical.</h1>
      <form
        className="mt-8 space-y-5 rounded-2xl border border-stone-200 bg-white p-6 shadow-sm"
        onSubmit={onSubmit}
      >
        <FormError error={error} />
        <label className="block text-sm font-semibold">
          Usuario o correo
          <input
            className={inputClass}
            value={usernameOrEmail}
            onChange={(event) => setUsernameOrEmail(event.target.value)}
            autoComplete="username"
            required
          />
        </label>
        <label className="block text-sm font-semibold">
          Contraseña
          <input
            className={inputClass}
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
            required
          />
        </label>
        <button
          className="w-full rounded-lg bg-ink px-4 py-3 font-semibold text-white hover:bg-moss disabled:opacity-50"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
      <p className="mt-5 text-center text-sm text-stone-600">
        ¿Aún no tienes cuenta?{' '}
        <Link className="font-semibold text-ink underline" to="/register">
          Crea una gratis.
        </Link>
      </p>
    </section>
  );
}

export function RegisterPage() {
  const { register, user } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', displayName: '' });
  const [error, setError] = useState<unknown>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await register(form);
      await navigate('/');
    } catch (submissionError) {
      setError(submissionError);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="mx-auto max-w-md">
      <p className="text-sm font-bold uppercase tracking-[0.2em] text-signal">Tu primera escucha</p>
      <h1 className="mt-2 font-display text-5xl">Empieza a recordar tu música.</h1>
      <form
        className="mt-8 space-y-5 rounded-2xl border border-stone-200 bg-white p-6 shadow-sm"
        onSubmit={onSubmit}
      >
        <FormError error={error} />
        <label className="block text-sm font-semibold">
          Nombre mostrado
          <input
            className={inputClass}
            value={form.displayName}
            onChange={(event) => setForm({ ...form, displayName: event.target.value })}
            autoComplete="name"
            maxLength={100}
            required
          />
        </label>
        <label className="block text-sm font-semibold">
          Usuario
          <input
            className={inputClass}
            value={form.username}
            onChange={(event) => setForm({ ...form, username: event.target.value })}
            autoComplete="username"
            maxLength={50}
            required
          />
        </label>
        <label className="block text-sm font-semibold">
          Correo electrónico
          <input
            className={inputClass}
            type="email"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            autoComplete="email"
            maxLength={255}
            required
          />
        </label>
        <label className="block text-sm font-semibold">
          Contraseña
          <input
            className={inputClass}
            type="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            autoComplete="new-password"
            minLength={8}
            maxLength={100}
            required
          />
        </label>
        <button
          className="w-full rounded-lg bg-ink px-4 py-3 font-semibold text-white hover:bg-moss disabled:opacity-50"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
      </form>
      <p className="mt-5 text-center text-sm text-stone-600">
        ¿Ya formas parte de MusicLog?{' '}
        <Link className="font-semibold text-ink underline" to="/login">
          Inicia sesión.
        </Link>
      </p>
    </section>
  );
}
