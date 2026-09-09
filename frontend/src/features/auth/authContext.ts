import { createContext } from 'react';
import type { UserProfile } from '../../api/types';
import type { LoginInput, RegisterInput } from './api';

export interface AuthContextValue {
  user: UserProfile | null;
  isRestoringSession: boolean;
  login: (input: LoginInput) => Promise<UserProfile>;
  register: (input: RegisterInput) => Promise<UserProfile>;
  replaceUser: (profile: UserProfile) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
