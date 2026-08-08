import { afterEach, describe, expect, it, vi } from 'vitest';
import { authStorage } from '../features/auth/authStorage';
import { api } from './http';

describe('api client', () => {
  afterEach(() => {
    authStorage.clear();
    vi.unstubAllGlobals();
  });

  it('sends the saved bearer token and returns typed JSON', async () => {
    authStorage.setToken('signed-token');
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: 'artist-1' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );
    vi.stubGlobal('fetch', fetchMock);

    await expect(api.get<{ id: string }>('/catalog/artists/artist-1')).resolves.toEqual({
      id: 'artist-1',
    });
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/catalog/artists/artist-1',
      expect.objectContaining({ headers: expect.any(Headers) }),
    );
    const headers = fetchMock.mock.calls[0][1]?.headers as Headers;
    expect(headers.get('Authorization')).toBe('Bearer signed-token');
  });

  it('exposes validation errors returned by the API', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            message: 'Validation failed',
            fieldErrors: { email: 'must be a well-formed email address' },
          }),
          { status: 400, headers: { 'Content-Type': 'application/json' } },
        ),
      ),
    );

    await expect(api.get('/auth/register')).rejects.toMatchObject({
      status: 400,
      fieldErrors: { email: 'must be a well-formed email address' },
    });
  });
});
