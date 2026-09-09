import { useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { ApiClientError } from '../api/http';
import type { UserProfile } from '../api/types';
import { FormError } from '../components/AsyncState';
import { accountApi } from '../features/account/api';
import type { AccountSettingsInput } from '../features/account/api';
import { useAuth } from '../features/auth/useAuth';

const inputClass =
  'mt-1 block w-full rounded-lg border-stone-300 bg-white px-3 py-2.5 text-ink shadow-sm outline-none focus:border-signal focus:ring-signal';

function formFromProfile(profile: UserProfile): AccountSettingsInput {
  return {
    displayName: profile.displayName,
    username: profile.username,
    newPassword: '',
    newPasswordConfirmation: '',
  };
}

function fieldErrorId(field: string) {
  return `account-${field}-error`;
}

interface PasswordFieldProps {
  id: 'newPassword' | 'newPasswordConfirmation';
  label: string;
  value: string;
  error?: string;
  onChange: (event: ChangeEvent<HTMLInputElement>) => void;
}

function PasswordField({ id, label, value, error, onChange }: PasswordFieldProps) {
  const [isVisible, setIsVisible] = useState(false);
  const describedBy = error ? fieldErrorId(id) : undefined;

  return (
    <div>
      <label className="block text-sm font-semibold" htmlFor={id}>
        {label}
      </label>
      <div className="relative mt-1">
        <input
          aria-describedby={describedBy}
          aria-invalid={Boolean(error)}
          autoComplete="new-password"
          className={`${inputClass} pr-12`}
          id={id}
          maxLength={100}
          onChange={onChange}
          type={isVisible ? 'text' : 'password'}
          value={value}
        />
        <button
          aria-label={`${isVisible ? 'Ocultar' : 'Mostrar'} ${label.toLowerCase()}`}
          aria-pressed={isVisible}
          className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md p-2 text-stone-500 hover:text-ink"
          onClick={() => setIsVisible((visible) => !visible)}
          type="button"
        >
          <svg aria-hidden="true" className="h-5 w-5" fill="none" viewBox="0 0 24 24">
            <path
              d="M2.5 12s3.5-5 9.5-5 9.5 5 9.5 5-3.5 5-9.5 5-9.5-5-9.5-5Z"
              stroke="currentColor"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="1.8"
            />
            <circle cx="12" cy="12" r="2.5" stroke="currentColor" strokeWidth="1.8" />
          </svg>
        </button>
      </div>
      {error ? (
        <p className="mt-1 text-sm text-red-700" id={fieldErrorId(id)}>
          {error}
        </p>
      ) : null}
    </div>
  );
}

export function AccountSettingsPage() {
  const { user, replaceUser } = useAuth();
  const [form, setForm] = useState<AccountSettingsInput>(() =>
    user ? formFromProfile(user) : { displayName: '', username: '', newPassword: '', newPasswordConfirmation: '' },
  );
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState<unknown>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!user) return null;

  const updateField = (field: keyof AccountSettingsInput, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
    setFieldErrors((current) => {
      const next = { ...current };
      delete next[field];
      return next;
    });
    setSubmitError(null);
    setSuccessMessage(null);
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};
    if (!form.displayName.trim()) {
      errors.displayName = 'El nombre mostrado es obligatorio.';
    } else if (form.displayName.length > 100) {
      errors.displayName = 'El nombre mostrado no puede superar 100 caracteres.';
    }
    if (!form.username.trim()) {
      errors.username = 'El nombre de usuario es obligatorio.';
    } else if (form.username.length > 50) {
      errors.username = 'El nombre de usuario no puede superar 50 caracteres.';
    }

    const passwordEmpty = form.newPassword.length === 0;
    const confirmationEmpty = form.newPasswordConfirmation.length === 0;
    if (passwordEmpty !== confirmationEmpty) {
      errors.newPassword = 'La nueva contraseña y su confirmación deben enviarse juntas.';
      errors.newPasswordConfirmation = 'La nueva contraseña y su confirmación deben enviarse juntas.';
    } else if (!passwordEmpty) {
      if (form.newPassword.length < 8 || form.newPassword.length > 100) {
        errors.newPassword = 'La nueva contraseña debe tener entre 8 y 100 caracteres.';
      }
      if (form.newPasswordConfirmation.length < 8 || form.newPasswordConfirmation.length > 100) {
        errors.newPasswordConfirmation = 'La confirmación debe tener entre 8 y 100 caracteres.';
      }
      if (form.newPassword !== form.newPasswordConfirmation) {
        errors.newPasswordConfirmation = 'Las contraseñas no coinciden.';
      }
    }

    setFieldErrors(errors);
    if (Object.keys(errors).length === 0) return true;
    setSubmitError(new ApiClientError(400, 'Revisa los campos marcados.'));
    return false;
  };

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitError(null);
    setSuccessMessage(null);
    if (!validateForm()) return;

    setIsSubmitting(true);
    try {
      const updatedProfile = await accountApi.update(form);
      replaceUser(updatedProfile);
      setForm(formFromProfile(updatedProfile));
      setFieldErrors({});
      setSuccessMessage('Los ajustes de tu cuenta se han guardado.');
    } catch (error) {
      if (error instanceof ApiClientError) {
        setFieldErrors(error.fieldErrors);
        setSubmitError(Object.keys(error.fieldErrors).length === 0 ? error : null);
      } else {
        setSubmitError(error);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="mx-auto max-w-2xl">
      <p className="text-sm font-bold uppercase tracking-[0.2em] text-signal">Tu cuenta</p>
      <h1 className="mt-2 font-display text-5xl">Ajustes de cuenta.</h1>
      <p className="mt-3 max-w-xl text-stone-600">
        Actualiza cómo apareces en MusicLog. Deja los campos de contraseña vacíos si no quieres
        cambiarla.
      </p>
      <form
        aria-label="Ajustes de cuenta"
        className="mt-8 space-y-5 rounded-2xl border border-stone-200 bg-white p-6 shadow-sm"
        onSubmit={onSubmit}
      >
        <FormError error={submitError} />
        {successMessage ? (
          <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-800" role="status">
            {successMessage}
          </p>
        ) : null}
        <label className="block text-sm font-semibold" htmlFor="displayName">
          Nombre mostrado
          <input
            aria-describedby={fieldErrors.displayName ? fieldErrorId('displayName') : undefined}
            aria-invalid={Boolean(fieldErrors.displayName)}
            className={inputClass}
            id="displayName"
            maxLength={100}
            onChange={(event) => updateField('displayName', event.target.value)}
            value={form.displayName}
          />
        </label>
        {fieldErrors.displayName ? (
          <p className="-mt-4 text-sm text-red-700" id={fieldErrorId('displayName')}>
            {fieldErrors.displayName}
          </p>
        ) : null}
        <label className="block text-sm font-semibold" htmlFor="username">
          Usuario
          <input
            aria-describedby={fieldErrors.username ? fieldErrorId('username') : undefined}
            aria-invalid={Boolean(fieldErrors.username)}
            autoComplete="username"
            className={inputClass}
            id="username"
            maxLength={50}
            onChange={(event) => updateField('username', event.target.value)}
            value={form.username}
          />
        </label>
        {fieldErrors.username ? (
          <p className="-mt-4 text-sm text-red-700" id={fieldErrorId('username')}>
            {fieldErrors.username}
          </p>
        ) : null}
        <div className="border-t border-stone-200 pt-5">
          <h2 className="font-display text-2xl">Cambiar contraseña</h2>
          <p className="mt-1 text-sm text-stone-600">Usa ambos campos para establecer una nueva.</p>
        </div>
        <PasswordField
          error={fieldErrors.newPassword}
          id="newPassword"
          label="Nueva contraseña"
          onChange={(event) => updateField('newPassword', event.target.value)}
          value={form.newPassword}
        />
        <PasswordField
          error={fieldErrors.newPasswordConfirmation}
          id="newPasswordConfirmation"
          label="Confirmar nueva contraseña"
          onChange={(event) => updateField('newPasswordConfirmation', event.target.value)}
          value={form.newPasswordConfirmation}
        />
        <button
          className="w-full rounded-lg bg-ink px-4 py-3 font-semibold text-white hover:bg-moss disabled:opacity-50"
          disabled={isSubmitting}
          type="submit"
        >
          {isSubmitting ? 'Guardando…' : 'Guardar cambios'}
        </button>
      </form>
    </section>
  );
}
