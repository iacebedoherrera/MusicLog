import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ApiClientError } from '../api/http';
import type { UserProfile } from '../api/types';
import { accountApi } from '../features/account/api';
import { RequireAuth } from '../features/auth/RequireAuth';
import type { AuthContextValue } from '../features/auth/authContext';
import { useAuth } from '../features/auth/useAuth';
import { AccountSettingsPage } from './AccountSettingsPage';

vi.mock('../features/account/api', () => ({
  accountApi: { update: vi.fn() },
}));

vi.mock('../features/auth/useAuth', () => ({
  useAuth: vi.fn(),
}));

const profile: UserProfile = {
  id: 'user-1',
  username: 'ana_music',
  displayName: 'Ana',
  bio: null,
  avatarUrl: null,
  createdAt: '2026-01-01T00:00:00Z',
};

const noop = () => undefined;

function authValue(user: UserProfile | null): AuthContextValue {
  return {
    user,
    isRestoringSession: false,
    login: vi.fn(),
    register: vi.fn(),
    replaceUser: noop,
    logout: noop,
  };
}

function renderPage() {
  return render(<AccountSettingsPage />);
}

describe('AccountSettingsPage', () => {
  beforeEach(() => {
    vi.mocked(useAuth).mockReturnValue(authValue(profile));
    vi.mocked(accountApi.update).mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('starts with the current identity and empty password fields', () => {
    renderPage();

    expect(screen.getByLabelText('Nombre mostrado')).toHaveValue('Ana');
    expect(screen.getByLabelText('Usuario')).toHaveValue('ana_music');
    expect(screen.getByLabelText('Nueva contraseña')).toHaveValue('');
    expect(screen.getByLabelText('Confirmar nueva contraseña')).toHaveValue('');
  });

  it('does not submit when the password pair differs', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.type(screen.getByLabelText('Nueva contraseña'), 'p'.repeat(8));
    await user.type(screen.getByLabelText('Confirmar nueva contraseña'), 'q'.repeat(8));
    await user.click(screen.getByRole('button', { name: 'Guardar cambios' }));

    expect(accountApi.update).not.toHaveBeenCalled();
    expect(screen.getByText('Las contraseñas no coinciden.')).toBeInTheDocument();
  });

  it('shows a field error for a password that is too short', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.type(screen.getByLabelText('Nueva contraseña'), 'short');
    await user.type(screen.getByLabelText('Confirmar nueva contraseña'), 'short');
    await user.click(screen.getByRole('button', { name: 'Guardar cambios' }));

    expect(accountApi.update).not.toHaveBeenCalled();
    expect(screen.getByText('La nueva contraseña debe tener entre 8 y 100 caracteres.')).toBeInTheDocument();
  });

  it('shows a username conflict returned by the API', async () => {
    const user = userEvent.setup();
    vi.mocked(accountApi.update).mockRejectedValue(
      new ApiClientError(409, 'El nombre de usuario ya está en uso.', {
        timestamp: '',
        status: 409,
        error: 'Conflict',
        message: 'El nombre de usuario ya está en uso.',
        path: '/api/users/me/account',
        fieldErrors: { username: 'El nombre de usuario ya está en uso.' },
      }),
    );
    renderPage();

    await user.clear(screen.getByLabelText('Usuario'));
    await user.type(screen.getByLabelText('Usuario'), 'taken_name');
    await user.click(screen.getByRole('button', { name: 'Guardar cambios' }));

    expect(await screen.findByText('El nombre de usuario ya está en uso.')).toBeInTheDocument();
    expect(accountApi.update).toHaveBeenCalledTimes(1);
  });

  it('submits a matching pair once and clears passwords after success', async () => {
    const user = userEvent.setup();
    const replaceUser = vi.fn();
    const updatedProfile = { ...profile, username: 'new_name', displayName: 'Nuevo nombre' };
    vi.mocked(useAuth).mockReturnValue({ ...authValue(profile), replaceUser });
    vi.mocked(accountApi.update).mockResolvedValue(updatedProfile);
    renderPage();

    await user.clear(screen.getByLabelText('Nombre mostrado'));
    await user.type(screen.getByLabelText('Nombre mostrado'), 'Nuevo nombre');
    await user.clear(screen.getByLabelText('Usuario'));
    await user.type(screen.getByLabelText('Usuario'), 'new_name');
    await user.type(screen.getByLabelText('Nueva contraseña'), 'p'.repeat(8));
    await user.type(screen.getByLabelText('Confirmar nueva contraseña'), 'p'.repeat(8));
    await user.click(screen.getByRole('button', { name: 'Guardar cambios' }));

    await waitFor(() => expect(accountApi.update).toHaveBeenCalledTimes(1));
    expect(accountApi.update).toHaveBeenCalledWith({
      displayName: 'Nuevo nombre',
      username: 'new_name',
      newPassword: 'p'.repeat(8),
      newPasswordConfirmation: 'p'.repeat(8),
    });
    expect(replaceUser).toHaveBeenCalledWith(updatedProfile);
    expect(screen.getByLabelText('Nueva contraseña')).toHaveValue('');
    expect(screen.getByLabelText('Confirmar nueva contraseña')).toHaveValue('');
  });

  it('toggles each password field accessibly without submitting', async () => {
    const user = userEvent.setup();
    renderPage();
    const password = screen.getByLabelText('Nueva contraseña');
    await user.type(password, 'p'.repeat(8));
    const showButton = screen.getByRole('button', { name: 'Mostrar nueva contraseña' });

    expect(password).toHaveAttribute('type', 'password');
    expect(showButton).toHaveAttribute('aria-pressed', 'false');
    await user.click(showButton);
    expect(password).toHaveAttribute('type', 'text');
    expect(password).toHaveValue('p'.repeat(8));
    expect(screen.getByRole('button', { name: 'Ocultar nueva contraseña' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    await user.click(screen.getByRole('button', { name: 'Ocultar nueva contraseña' }));
    expect(password).toHaveAttribute('type', 'password');
    expect(accountApi.update).not.toHaveBeenCalled();
  });

  it('redirects an unauthenticated visitor before any save request', () => {
    vi.mocked(useAuth).mockReturnValue(authValue(null));

    render(
      <MemoryRouter initialEntries={['/settings/account']}>
        <Routes>
          <Route
            path="/settings/account"
            element={
              <RequireAuth>
                <AccountSettingsPage />
              </RequireAuth>
            }
          />
          <Route path="/login" element={<p>Inicio de sesión</p>} />
        </Routes>
      </MemoryRouter>,
    );

    expect(screen.getByText('Inicio de sesión')).toBeInTheDocument();
    expect(accountApi.update).not.toHaveBeenCalled();
  });
});
