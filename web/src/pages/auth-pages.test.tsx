import { beforeEach, describe, expect, it } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthProvider } from '../app/providers/AuthProvider';
import { AppRouter } from '../app/router';

beforeEach(() => {
  localStorage.clear();
});

describe('auth pages', () => {
  it('shows validation errors on the login form', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <AppRouter />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: /войти/i }));

    expect(screen.getByRole('alert')).toHaveTextContent(/Введите email/i);
  });

  it('registers a user and redirects to feed', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/register']}>
        <AuthProvider>
          <AppRouter />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText(/email/i), 'new@example.com');
    await user.type(screen.getByLabelText(/^пароль$/i), 'password123');
    await user.type(screen.getByLabelText(/подтвердите пароль/i), 'password123');
    await user.click(screen.getByRole('button', { name: /создать аккаунт/i }));

    await waitFor(() => expect(screen.getByRole('heading', { name: /Лента/i })).toBeInTheDocument());
  });

  it('logs in a returning user and redirects to feed', async () => {
    localStorage.setItem(
      'matchmaker:v1:users',
      JSON.stringify([
        { id: 'user-1', email: 'test@example.com', password: 'password123', createdAt: '2026-01-01T00:00:00.000Z' },
      ]),
    );

    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <AppRouter />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/^пароль$/i), 'password123');
    await user.click(screen.getByRole('button', { name: /войти/i }));

    await waitFor(() => expect(screen.getByRole('heading', { name: /Лента/i })).toBeInTheDocument());
  });

  it('returns a user to the full protected URL after login', async () => {
    localStorage.setItem(
      'matchmaker:v1:users',
      JSON.stringify([
        { id: 'user-1', email: 'test@example.com', password: 'password123', createdAt: '2026-01-01T00:00:00.000Z' },
      ]),
    );
    const user = userEvent.setup();

    render(
      <MemoryRouter
        initialEntries={[
          {
            pathname: '/login',
            state: { from: { pathname: '/chat/abc', search: '?tab=info', hash: '#messages' } },
          },
        ]}
      >
        <AuthProvider>
          <AppRouter />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/^пароль$/i), 'password123');
    await user.click(screen.getByRole('button', { name: /войти/i }));

    await waitFor(() => expect(screen.getByText(/matchId: abc/i)).toBeInTheDocument());
  });

  it('redirects an authenticated user away from the login page', async () => {
    localStorage.setItem(
      'matchmaker:v1:users',
      JSON.stringify([
        { id: 'user-1', email: 'test@example.com', password: 'password123', createdAt: '2026-01-01T00:00:00.000Z' },
      ]),
    );
    localStorage.setItem('matchmaker:v1:session', JSON.stringify({ userId: 'user-1' }));

    render(
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <AppRouter />
        </AuthProvider>
      </MemoryRouter>,
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: /Лента/i })).toBeInTheDocument());
  });

  it('shows auth errors from the provider on the login form', async () => {
    localStorage.setItem(
      'matchmaker:v1:users',
      JSON.stringify([
        { id: 'user-1', email: 'test@example.com', password: 'password123', createdAt: '2026-01-01T00:00:00.000Z' },
      ]),
    );

    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <AppRouter />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText(/email/i), 'TEST@example.com');
    await user.type(screen.getByLabelText(/^пароль$/i), 'wrong-password');
    await user.click(screen.getByRole('button', { name: /войти/i }));

    await waitFor(() =>
      expect(screen.getByRole('alert')).toHaveTextContent(/Неверный email или пароль/i),
    );
  });

  it('shows auth errors from the provider on the register form', async () => {
    localStorage.setItem(
      'matchmaker:v1:users',
      JSON.stringify([
        { id: 'user-1', email: 'test@example.com', password: 'password123', createdAt: '2026-01-01T00:00:00.000Z' },
      ]),
    );

    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/register']}>
        <AuthProvider>
          <AppRouter />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText(/email/i), ' TEST@example.com ');
    await user.type(screen.getByLabelText(/^пароль$/i), 'password123');
    await user.type(screen.getByLabelText(/подтвердите пароль/i), 'password123');
    await user.click(screen.getByRole('button', { name: /создать аккаунт/i }));

    await waitFor(() =>
      expect(screen.getByRole('alert')).toHaveTextContent(/Пользователь с таким email уже существует/i),
    );
  });
});