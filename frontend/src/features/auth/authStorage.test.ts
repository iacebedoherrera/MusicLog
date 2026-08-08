import { beforeEach, describe, expect, it } from 'vitest';
import { authStorage } from './authStorage';

describe('authStorage', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it('persists and clears the development token', () => {
    expect(authStorage.getToken()).toBeNull();
    authStorage.setToken('jwt-value');
    expect(authStorage.getToken()).toBe('jwt-value');
    authStorage.clear();
    expect(authStorage.getToken()).toBeNull();
  });
});
