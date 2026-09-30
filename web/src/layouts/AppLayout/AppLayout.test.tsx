import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AppLayout } from './AppLayout';
import { AuthProvider } from '../../app/providers/AuthProvider';
import { ProtectedRoute } from '../../app/providers/ProtectedRoute';
import type { AuthService } from '../../shared/lib/auth/auth-types';

// Мокаем сервис аутентификации, чтобы инициализация происходила синхронно
vi.mock('../../shared/lib/auth/mock-auth-service', () => ({
  mockAuthService: {
    getCurrentUser: vi.fn().mockResolvedValue({
      id: 'user-1',
      email: 'test@example.com',
    }),
    login: vi.fn().mockResolvedValue({
      id: 'user-1',
      email: 'test@example.com',
    }),
    register: vi.fn().mockResolvedValue({
      id: 'user-1',
      email: 'test@example.com',
    }),
    logout: vi.fn().mockResolvedValue(undefined),
  },
}));

describe('AppLayout', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  function renderAppLayout(childElement: React.ReactElement) {
    return render(
      <MemoryRouter initialEntries={['/protected']}>
        <AuthProvider>
          <Routes>
            <Route path="/protected" element={<AppLayout />}>
              <Route index element={childElement} />
            </Route>
            <Route path="/login" element={<div>Login Page</div>} />
            <Route path="/" element={<div>Home Page</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );
  }

  it('renders header with logo and navigation when user is authenticated', async () => {
    renderAppLayout(<div data-testid="child-content">Child content</div>);

    await screen.findByText('Matchmaker');
    expect(screen.getByRole('link', { name: 'Feed' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Projects' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Profile' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Matches' })).toBeInTheDocument();
    // Chat is not in navigation — need real matchId for the link
  });

  it('renders logout button when user is authenticated', async () => {
    renderAppLayout(<div data-testid="child-content">Child content</div>);

    expect(await screen.findByRole('button', { name: 'Exit' })).toBeInTheDocument();
  });

  it('renders Outlet for children when user is authenticated', async () => {
    renderAppLayout(<div data-testid="child-content">Child content</div>);

    expect(await screen.findByTestId('child-content')).toBeInTheDocument();
  });

  it('logout delegates to the auth service', async () => {
    const user = userEvent.setup();
    renderAppLayout(<div data-testid="child-content">Child content</div>);

    const logoutButton = await screen.findByRole('button', { name: 'Exit' });
    await user.click(logoutButton);

    const { mockAuthService } = await import('../../shared/lib/auth/mock-auth-service');
    expect(mockAuthService.logout).toHaveBeenCalled();
  });

  it('shows a logout error instead of creating an unhandled rejection', async () => {
    const user = userEvent.setup();
    const { mockAuthService } = await import('../../shared/lib/auth/mock-auth-service');
    vi.mocked(mockAuthService.logout).mockRejectedValueOnce(new Error('logout failed'));

    renderAppLayout(<div data-testid="child-content">Child content</div>);

    await user.click(await screen.findByRole('button', { name: 'Exit' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/Не удалось выйти/i);
  });

  it('redirects to login after a successful logout in the protected router', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/feed']}>
        <AuthProvider>
          <Routes>
            <Route
              path="/feed"
              element={
                <ProtectedRoute>
                  <AppLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<div>Feed content</div>} />
            </Route>
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.click(await screen.findByRole('button', { name: 'Exit' }));

    await waitFor(() => expect(screen.getByText('Login Page')).toBeInTheDocument());
  });
});
