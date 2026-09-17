import { beforeEach, describe, expect, it } from 'vitest';
import { MemoryRouter, Navigate, Route, Routes } from 'react-router-dom';
import { render, screen, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthProvider';
import { ProtectedRoute } from './ProtectedRoute';

beforeEach(() => {
  localStorage.clear();
});

function AuthStatus() {
  const { isInitialized, user, authError } = useAuth();

  return (
    <div>
      <p>initialized: {isInitialized ? 'yes' : 'no'}</p>
      <p>user: {user?.email ?? 'none'}</p>
      <p>error: {authError ?? 'none'}</p>
    </div>
  );
}

describe('AuthProvider', () => {
  it('restores an existing session on mount', async () => {
    localStorage.setItem(
      'matchmaker:v1:users',
      JSON.stringify([
        { id: 'user-1', email: 'test@example.com', password: 'password123', createdAt: '2026-01-01T00:00:00.000Z' },
      ]),
    );
    localStorage.setItem('matchmaker:v1:session', JSON.stringify({ userId: 'user-1' }));

    render(
      <MemoryRouter>
        <AuthProvider>
          <AuthStatus />
        </AuthProvider>
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText(/initialized: yes/i)).toBeInTheDocument());
    expect(screen.getByText(/user: test@example.com/i)).toBeInTheDocument();
    expect(screen.getByText(/error: none/i)).toBeInTheDocument();
  });

  it('redirects guests from protected routes to login after initialization', async () => {
    render(
      <MemoryRouter initialEntries={['/feed']}>
        <AuthProvider>
          <Routes>
            <Route
              path="/feed"
              element={
                <ProtectedRoute>
                  <div>feed content</div>
                </ProtectedRoute>
              }
            />
            <Route path="/login" element={<div>login page</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText(/login page/i)).toBeInTheDocument());
  });

  it('lets authenticated users stay inside protected routes', async () => {
    localStorage.setItem(
      'matchmaker:v1:users',
      JSON.stringify([
        { id: 'user-1', email: 'test@example.com', password: 'password123', createdAt: '2026-01-01T00:00:00.000Z' },
      ]),
    );
    localStorage.setItem('matchmaker:v1:session', JSON.stringify({ userId: 'user-1' }));

    render(
      <MemoryRouter initialEntries={['/feed']}>
        <AuthProvider>
          <Routes>
            <Route
              path="/feed"
              element={
                <ProtectedRoute>
                  <div>feed content</div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText(/feed content/i)).toBeInTheDocument());
  });

  it('shows a storage unavailable error in the UI when localStorage cannot be accessed', async () => {
    const originalStorage = Object.getOwnPropertyDescriptor(window, 'localStorage');

    Object.defineProperty(window, 'localStorage', {
      configurable: true,
      get() {
        throw new Error('denied');
      },
    });

    render(
      <MemoryRouter>
        <AuthProvider>
          <AuthStatus />
        </AuthProvider>
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByText(/initialized: yes/i)).toBeInTheDocument());
    expect(screen.getByText(/user: none/i)).toBeInTheDocument();
    expect(screen.getByText(/error: .*недоступ/i)).toBeInTheDocument();

    if (originalStorage) {
      Object.defineProperty(window, 'localStorage', originalStorage);
    }
  });
});