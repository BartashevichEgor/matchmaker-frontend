import { afterEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CatalogPage } from './CatalogPage';

const token = { access_token: 'access-token', refresh_token: 'refresh-token' };
const bothCardsComplete = {
  mode: 'project',
  profile: { complete: true },
  project: { complete: true },
};

describe('CatalogPage project API integration', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loads and displays projects from the API', async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify(bothCardsComplete), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify([{
        id: 7,
        owner_id: 3,
        title: 'Matchmaker',
        description: 'Платформа для поиска команды.',
        status: 'open',
        created_at: '2026-11-20T10:00:00Z',
        skills: [{ skill_id: 2, weight: 3 }],
        role_ids: [4],
      }]), { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);
    localStorage.setItem('matchmaker:v1:tokens', JSON.stringify(token));

    render(<CatalogPage />);
    expect(await screen.findByRole('tablist', { name: 'Тип каталога' })).toBeInTheDocument();
    await user.click(await screen.findByRole('tab', { name: 'Проекты' }));

    expect(await screen.findByRole('heading', { name: 'Matchmaker' })).toBeInTheDocument();
    expect(screen.getByText('Платформа для поиска команды.')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      'http://127.0.0.1:8000/api/v1/projects',
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: 'Bearer access-token' }) }),
    );
  });

  it('shows only Specialists when the project card is the only completed card', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      mode: 'project',
      profile: { complete: false },
      project: { complete: true },
    }), { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);
    localStorage.setItem('matchmaker:v1:tokens', JSON.stringify(token));

    render(<CatalogPage />);

    expect(await screen.findByRole('heading', { name: 'Алексей Ковалёв' })).toBeInTheDocument();
    expect(screen.queryByRole('tablist')).not.toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('shows only Projects when the developer profile is the only completed card', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify({
        mode: 'developer',
        profile: { complete: true },
        project: { complete: false },
      }), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify([]), { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);
    localStorage.setItem('matchmaker:v1:tokens', JSON.stringify(token));

    render(<CatalogPage />);

    expect(await screen.findByText('В каталоге пока нет проектов.')).toBeInTheDocument();
    expect(screen.queryByRole('tablist')).not.toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      'http://127.0.0.1:8000/api/v1/projects',
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: 'Bearer access-token' }) }),
    );
  });

  it('lets the user retry when onboarding status cannot be loaded', async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify({ detail: 'Сервис недоступен' }), { status: 503 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        mode: 'developer',
        profile: { complete: true },
        project: { complete: false },
      }), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify([]), { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);
    localStorage.setItem('matchmaker:v1:tokens', JSON.stringify(token));

    render(<CatalogPage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Сервис недоступен');
    await user.click(screen.getByRole('button', { name: 'Повторить' }));

    expect(await screen.findByText('В каталоге пока нет проектов.')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });

  it('shows a recoverable error when the catalog request fails', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify(bothCardsComplete), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ detail: 'Сервис недоступен' }), { status: 503 }));
    const user = userEvent.setup();
    vi.stubGlobal('fetch', fetchMock);
    localStorage.setItem('matchmaker:v1:tokens', JSON.stringify(token));

    render(<CatalogPage />);
    await user.click(await screen.findByRole('tab', { name: 'Проекты' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Сервис недоступен'));
    expect(screen.getByRole('button', { name: 'Повторить' })).toBeInTheDocument();
  });
});
