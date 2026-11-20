import { afterEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CatalogPage } from './CatalogPage';

const token = { access_token: 'access-token', refresh_token: 'refresh-token' };

describe('CatalogPage project API integration', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loads and displays projects from the API', async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([{
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
    await user.click(screen.getByRole('tab', { name: 'Проекты' }));

    expect(await screen.findByRole('heading', { name: 'Matchmaker' })).toBeInTheDocument();
    expect(screen.getByText('Платформа для поиска команды.')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      'http://127.0.0.1:8000/api/v1/projects',
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: 'Bearer access-token' }) }),
    );
  });

  it('shows a recoverable error when the catalog request fails', async () => {
    const user = userEvent.setup();
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ detail: 'Сервис недоступен' }), { status: 503 })));
    localStorage.setItem('matchmaker:v1:tokens', JSON.stringify(token));

    render(<CatalogPage />);
    await user.click(screen.getByRole('tab', { name: 'Проекты' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Сервис недоступен'));
    expect(screen.getByRole('button', { name: 'Повторить' })).toBeInTheDocument();
  });
});
