import { useCallback, useEffect, useMemo, useState } from 'react';
import type { PropsWithChildren } from 'react';
import { ApiClientError } from '../../api/http';
import type { UserProfile } from '../../api/types';
import { authApi } from './api';
import type { LoginInput, RegisterInput } from './api';
import { AuthContext } from './authContext';
import { authStorage } from './authStorage';

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isRestoringSession, setIsRestoringSession] = useState(true);

  useEffect(() => {
    let active = true;

    const restore = async () => {
      if (!authStorage.getToken()) {
        if (active) setIsRestoringSession(false);
        return;
      }

      try {
        const profile = await authApi.me();
        if (active) setUser(profile);
      } catch (error) {
        if (error instanceof ApiClientError && error.status === 401) {
          authStorage.clear();
        }
      } finally {
        if (active) setIsRestoringSession(false);
      }
    };

    void restore();
    return () => {
      active = false;
    };
  }, []);

  const login = useCallback(async (input: LoginInput) => {
    const result = await authApi.login(input);
    authStorage.setToken(result.token);
    try {
      const profile = await authApi.me();
      setUser(profile);
      return profile;
    } catch (error) {
      authStorage.clear();
      throw error;
    }
  }, []);

  const register = useCallback(
    async (input: RegisterInput) => {
      await authApi.register(input);
      return login({ usernameOrEmail: input.username, password: input.password });
    },
    [login],
  );

  const logout = useCallback(() => {
    authStorage.clear();
    setUser(null);
  }, []);

  const replaceUser = useCallback((profile: UserProfile) => {
    setUser(profile);
  }, []);

  const value = useMemo(
    () => ({ user, isRestoringSession, login, register, replaceUser, logout }),
    [isRestoringSession, login, logout, register, replaceUser, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
