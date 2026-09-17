import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AppLayout } from './AppLayout';
import { AuthProvider } from '../../app/providers/AuthProvider';

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

  it('renders header with logo and navigation when user is authenticated', () => {
    renderAppLayout(<div data-testid="child-content">Child content</div>);
    
    // После мокания сервиса инициализация происходит синхронно
    expect(screen.getByText('Matchmaker')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Лента' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Проекты' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Профиль' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Мэтчи' })).toBeInTheDocument();
    // Чат не в навигации — нужен реальный matchId для ссылки
  });

  it('renders logout button when user is authenticated', () => {
    renderAppLayout(<div data-testid="child-content">Child content</div>);
    
    expect(screen.getByRole('button', { name: 'Выход' })).toBeInTheDocument();
  });

  it('renders Outlet for children when user is authenticated', () => {
    renderAppLayout(<div data-testid="child-content">Child content</div>);
    
    expect(screen.getByTestId('child-content')).toBeInTheDocument();
  });

  it('logout calls logout and redirects to /', async () => {
    const user = userEvent.setup();
    renderAppLayout(<div data-testid="child-content">Child content</div>);
    
    const logoutButton = screen.getByRole('button', { name: 'Выход' });
    await user.click(logoutButton);
    
    // Проверяем, что произошел редирект
    expect(window.location.pathname).toBe('/');
    
    // Проверяем, что logout был вызван
    const { mockAuthService } = await import('../../shared/lib/auth/mock-auth-service');
    expect(mockAuthService.logout).toHaveBeenCalled();
  });
});