import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import type { UserProfile } from '../api/types';
import { useAuth } from '../features/auth/useAuth';
import { AppLayout } from './AppLayout';

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

function renderLayout() {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route element={<AppLayout />} path="/">
          <Route index element={<p>Descubrir</p>} />
          <Route path="settings/account" element={<p>Ajustes de cuenta</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('AppLayout', () => {
  beforeEach(() => {
    vi.mocked(useAuth).mockReturnValue({
      user: profile,
      isRestoringSession: false,
      login: vi.fn(),
      register: vi.fn(),
      replaceUser: vi.fn(),
      logout: vi.fn(),
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('opens account settings from the authenticated navigation', async () => {
    const user = userEvent.setup();
    renderLayout();

    await user.click(screen.getByRole('link', { name: 'Cuenta' }));

    expect(screen.getByText('Ajustes de cuenta')).toBeInTheDocument();
  });

  it('does not show the account link for a closed session', () => {
    vi.mocked(useAuth).mockReturnValue({
      user: null,
      isRestoringSession: false,
      login: vi.fn(),
      register: vi.fn(),
      replaceUser: vi.fn(),
      logout: vi.fn(),
    });
    renderLayout();

    expect(screen.queryByRole('link', { name: 'Cuenta' })).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Entrar' })).toBeInTheDocument();
  });
});
