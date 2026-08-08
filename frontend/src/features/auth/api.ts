import { api } from '../../api/http';
import type { LoginResponse, UserProfile } from '../../api/types';

export interface LoginInput {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterInput {
  username: string;
  email: string;
  password: string;
  displayName: string;
}

export const authApi = {
  login(input: LoginInput): Promise<LoginResponse> {
    return api.post<LoginResponse>('/auth/login', input);
  },
  register(input: RegisterInput): Promise<UserProfile> {
    return api.post<UserProfile>('/auth/register', input);
  },
  me(): Promise<UserProfile> {
    return api.get<UserProfile>('/users/me');
  },
};
