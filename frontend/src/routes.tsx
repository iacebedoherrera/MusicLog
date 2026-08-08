import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './components/AppLayout';
import { RequireAuth } from './features/auth/RequireAuth';
import { LoginPage, RegisterPage } from './pages/AuthPages';
import { AlbumDetailPage, ArtistDetailPage, TrackDetailPage } from './pages/CatalogDetailPages';
import { CatalogSearchPage } from './pages/CatalogSearchPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { ReviewDetailPage, ReviewFormPage } from './pages/ReviewPages';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <CatalogSearchPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      { path: 'artists/:mbid', element: <ArtistDetailPage /> },
      { path: 'albums/:mbid', element: <AlbumDetailPage /> },
      { path: 'tracks/:mbid', element: <TrackDetailPage /> },
      { path: 'reviews/:id', element: <ReviewDetailPage /> },
      {
        path: 'reviews/new',
        element: (
          <RequireAuth>
            <ReviewFormPage />
          </RequireAuth>
        ),
      },
      {
        path: 'reviews/:id/edit',
        element: (
          <RequireAuth>
            <ReviewFormPage />
          </RequireAuth>
        ),
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
