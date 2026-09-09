import { api } from '../../api/http';
import type { UserProfile } from '../../api/types';

export interface AccountSettingsInput {
  displayName: string;
  username: string;
  newPassword: string;
  newPasswordConfirmation: string;
}

export const accountApi = {
  update(input: AccountSettingsInput): Promise<UserProfile> {
    return api.put<UserProfile>('/users/me/account', input);
  },
};
