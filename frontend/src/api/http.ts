import { authStorage } from '../features/auth/authStorage';
import type { ApiErrorResponse } from './types';

const configuredApiUrl = import.meta.env.VITE_API_URL ?? '/api';
const apiUrl = configuredApiUrl.endsWith('/') ? configuredApiUrl.slice(0, -1) : configuredApiUrl;

export class ApiClientError extends Error {
  readonly status: number;
  readonly fieldErrors: Record<string, string>;
  readonly payload: ApiErrorResponse | undefined;

  constructor(status: number, message: string, payload?: ApiErrorResponse) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.fieldErrors = payload?.fieldErrors ?? {};
    this.payload = payload;
  }
}

async function readBody(response: Response): Promise<unknown> {
  if (response.status === 204) {
    return undefined;
  }

  const contentType = response.headers.get('content-type') ?? '';
  if (contentType.includes('application/json')) {
    return response.json();
  }

  return response.text();
}

function errorFromResponse(status: number, body: unknown): ApiClientError {
  if (typeof body === 'object' && body !== null && 'message' in body) {
    const payload = body as ApiErrorResponse;
    return new ApiClientError(
      status,
      payload.message || 'La solicitud no se pudo completar.',
      payload,
    );
  }

  const message = typeof body === 'string' && body ? body : 'La solicitud no se pudo completar.';
  return new ApiClientError(status, message);
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = authStorage.getToken();
  const headers = new Headers(init.headers);
  headers.set('Accept', 'application/json');

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (init.body && !(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  let response: Response;
  try {
    response = await fetch(`${apiUrl}${path}`, { ...init, headers });
  } catch {
    throw new ApiClientError(0, 'No se ha podido conectar con MusicLog. Inténtalo de nuevo.');
  }

  const body = await readBody(response);
  if (!response.ok) {
    throw errorFromResponse(response.status, body);
  }

  return body as T;
}

export const api = {
  get<T>(path: string): Promise<T> {
    return request<T>(path);
  },
  post<T>(path: string, body?: unknown): Promise<T> {
    return request<T>(path, {
      method: 'POST',
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  },
  put<T>(path: string, body: unknown): Promise<T> {
    return request<T>(path, { method: 'PUT', body: JSON.stringify(body) });
  },
  delete(path: string): Promise<void> {
    return request<void>(path, { method: 'DELETE' });
  },
};
