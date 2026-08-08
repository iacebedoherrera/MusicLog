const TOKEN_STORAGE_KEY = 'musiclog.auth.token';

/**
 * Development-only token persistence boundary.
 *
 * TODO(security): replace this implementation with a cookie-based session using
 * Secure, HttpOnly, SameSite cookies and a refresh-token flow before production.
 */
export const authStorage = {
  getToken(): string | null {
    try {
      return window.localStorage.getItem(TOKEN_STORAGE_KEY);
    } catch {
      return null;
    }
  },

  setToken(token: string): void {
    window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
  },

  clear(): void {
    window.localStorage.removeItem(TOKEN_STORAGE_KEY);
  },
};
