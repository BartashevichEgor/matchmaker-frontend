import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AppLayout } from './AppLayout';
import { AuthProvider } from '../../app/providers/AuthProvider';

function setMockSession() {
  localStorage.setItem(
    'matchmaker:v1:users',
    JSON.stringify([
      { id: 'user-1', email: 'test@example.com', password: 'password123', createdAt: '2026-01-01T00:00:00.000Z' },
    ])
  );
  localStorage.setItem('matchmaker:v1:session', JSON.stringify({ userId: 'user-1' }));
}

function renderAppLayoutWithAuth(childElement: React.ReactElement) {
  setMockSession();
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

describe('AppLayout', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('renders header with logo and navigation when user is authenticated', async () => {
    renderAppLayoutWithAuth(<div data-testid="child-content">Child content</div>);
    
    // Wait for auth initialization and app render
    await waitFor(() => {
      expect(screen.getByText('Matchmaker')).toBeInTheDocument();
    }, { timeout: 3000 });
    
    expect(screen.getByRole('link', { name: 'Лента' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Проекты' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Профиль' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Мэтчи' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Чат' })).toBeInTheDocument();
  });

  it('renders logout button when user is authenticated', async () => {
    renderAppLayoutWithAuth(<div data-testid="child-content">Child content</div>);
    
    // Wait for auth initialization
    await waitFor(() => {
      expect(screen.getByText('Matchmaker')).toBeInTheDocument();
    }, { timeout: 3000 });
    
    const logoutButton = screen.getByRole('button', { name: 'Выход' });
    expect(logoutButton).toBeInTheDocument();
  });

  it('renders Outlet for children when user is authenticated', async () => {
    renderAppLayoutWithAuth(<div data-testid="child-content">Child content</div>);
    
    // Wait for auth initialization
    await waitFor(() => {
      expect(screen.getByText('Matchmaker')).toBeInTheDocument();
    }, { timeout: 3000 });
    
    const child = screen.getByTestId('child-content');
    expect(child).toBeInTheDocument();
  });

  it('logout calls logout and redirects to /', async () => {
    const user = userEvent.setup();
    renderAppLayoutWithAuth(<div data-testid="child-content">Child content</div>);
    
    // Wait for auth initialization
    await waitFor(() => {
      expect(screen.getByText('Matchmaker')).toBeInTheDocument();
    }, { timeout: 3000 });
    
    const logoutButton = screen.getByRole('button', { name: 'Выход' });
    await user.click(logoutButton);
    
    // Wait for navigation to complete
    await waitFor(() => {
      expect(window.location.pathname).toBe('/');
    }, { timeout: 3000 });
  });
});