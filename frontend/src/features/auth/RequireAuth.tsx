import type { PropsWithChildren } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { LoadingState } from '../../components/AsyncState';
import { useAuth } from './useAuth';

export function RequireAuth({ children }: PropsWithChildren) {
  const { user, isRestoringSession } = useAuth();
  const location = useLocation();

  if (isRestoringSession) {
    return <LoadingState label="Recuperando tu sesión…" />;
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <>{children}</>;
}
