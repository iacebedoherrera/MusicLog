import { afterEach, describe, expect, it } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { useEffect } from 'react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import type { UserProfile } from '../../api/types';
import { AppLayout } from '../../components/AppLayout';
import { authStorage } from './authStorage';
import { AuthProvider } from './AuthProvider';
import { useAuth } from './useAuth';

const updatedProfile: UserProfile = {
  id: 'user-1',
  username: 'nuevo_usuario',
  displayName: 'Nombre actualizado',
  bio: null,
  avatarUrl: null,
  createdAt: '2026-01-01T00:00:00Z',
};

function ReplaceUserOnMount() {
  const { replaceUser } = useAuth();
  useEffect(() => {
    replaceUser(updatedProfile);
  }, [replaceUser]);
  return null;
}

describe('AuthProvider', () => {
  afterEach(() => {
    authStorage.clear();
  });

  it('updates the authenticated header when the current profile is replaced', async () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <AuthProvider>
          <Routes>
            <Route
              path="*"
              element={
                <>
                  <ReplaceUserOnMount />
                  <AppLayout />
                </>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText('Hola, Nombre actualizado')).toBeInTheDocument());
    expect(screen.getByRole('link', { name: 'Cuenta' })).toHaveAttribute('href', '/settings/account');
  });
});
