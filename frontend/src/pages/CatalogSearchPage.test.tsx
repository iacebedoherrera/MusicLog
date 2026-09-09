import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { CatalogSearchPage } from './CatalogSearchPage';

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <CatalogSearchPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('CatalogSearchPage', () => {
  it('does not show a loading state before a search starts', () => {
    renderPage();

    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('keeps the selector arrow clear of its text', () => {
    renderPage();

    const selector = screen.getByRole('combobox', { name: 'Tipo de búsqueda' });

    expect(selector).toHaveClass('appearance-none', 'pr-10');
    expect(selector.parentElement).toHaveClass('relative');
  });
});
