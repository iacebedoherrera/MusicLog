import { afterEach, describe, expect, it, vi } from 'vitest';
import { authStorage } from '../../features/auth/authStorage';
import { accountApi } from './api';

const input = {
  displayName: 'Ana',
  username: 'ana_music',
  newPassword: 'p'.repeat(8),
  newPasswordConfirmation: 'p'.repeat(8),
};

describe('account api', () => {
  afterEach(() => {
    authStorage.clear();
    vi.unstubAllGlobals();
  });

  it('sends the account update to the documented endpoint', async () => {
    const response = {
      id: 'user-1',
      username: input.username,
      displayName: input.displayName,
      bio: null,
      avatarUrl: null,
      createdAt: '2026-01-01T00:00:00Z',
    };
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify(response), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );
    vi.stubGlobal('fetch', fetchMock);

    await expect(accountApi.update(input)).resolves.toEqual(response);

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/users/me/account',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify(input) }),
    );
  });

  it('keeps field errors from an account conflict', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            message: 'El nombre de usuario ya está en uso.',
            fieldErrors: { username: 'El nombre de usuario ya está en uso.' },
          }),
          { status: 409, headers: { 'Content-Type': 'application/json' } },
        ),
      ),
    );

    await expect(accountApi.update({ ...input, newPassword: '', newPasswordConfirmation: '' }))
      .rejects.toMatchObject({
        status: 409,
        fieldErrors: { username: 'El nombre de usuario ya está en uso.' },
      });
  });
});
