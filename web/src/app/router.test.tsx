import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AppRouter } from './router';

describe('AppRouter', () => {
  it('renders the home page on /', () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <AppRouter />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: /Подбор проектов и людей для совместной работы/i })).toBeInTheDocument();
  });

  it('renders the login page on /login', () => {
    render(
      <MemoryRouter initialEntries={['/login']}>
        <AppRouter />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: /Войти в аккаунт/i })).toBeInTheDocument();
  });

  it('renders the register page on /register', () => {
    render(
      <MemoryRouter initialEntries={['/register']}>
        <AppRouter />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: /Создать аккаунт/i })).toBeInTheDocument();
  });

  it('renders the feed placeholder on /feed', () => {
    render(
      <MemoryRouter initialEntries={['/feed']}>
        <AppRouter />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: /Лента/i })).toBeInTheDocument();
    expect(screen.getByText(/В разработке/i)).toBeInTheDocument();
  });

  it('renders the chat placeholder with matchId on /chat/:matchId', () => {
    render(
      <MemoryRouter initialEntries={['/chat/abc']}>
        <AppRouter />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: /Чат/i })).toBeInTheDocument();
    expect(screen.getByText(/matchId: abc/i)).toBeInTheDocument();
  });

  it('renders the public 404 page on unknown routes', () => {
    render(
      <MemoryRouter initialEntries={['/unknown']}>
        <AppRouter />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: /Страница не найдена/i })).toBeInTheDocument();
  });
});
