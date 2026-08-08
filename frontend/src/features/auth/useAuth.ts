import { useContext } from 'react';
import type { AuthContextValue } from './authContext';
import { AuthContext } from './authContext';

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider.');
  }
  return context;
}
