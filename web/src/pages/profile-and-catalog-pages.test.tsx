import { describe, expect, it } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CatalogPage } from './catalog/CatalogPage';
import { CreateProfilePage } from './profile/CreateProfilePage';

describe('profile and catalog interactions', () => {
  it('updates photo initials and removes a skill from the profile form', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/profile/create?step=1']}>
        <CreateProfilePage />
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText('Имя *'), 'Анна');
    await user.click(screen.getByRole('button', { name: 'Фото и о себе · необязательно' }));
    expect(screen.getByRole('button', { name: 'Добавить фото' })).toHaveTextContent('А');

    await user.type(screen.getByLabelText('Фамилия *'), 'Иванова');
    expect(screen.getByRole('button', { name: 'Добавить фото' })).toHaveTextContent('АИ');

    await user.click(screen.getByRole('button', { name: /Далее: навыки и образование/i }));
    await user.type(screen.getByPlaceholderText('Добавить навык'), 'React');
    await user.click(screen.getByRole('button', { name: 'Добавить навык' }));
    expect(screen.getByText('React')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Удалить навык React' }));
    expect(screen.queryByText('React')).not.toBeInTheDocument();
  });

  it('opens the related fields in profile creation and catalog filters', async () => {
    const user = userEvent.setup();

    const { unmount } = render(
      <MemoryRouter initialEntries={['/profile/create?step=2']}>
        <CreateProfilePage />
      </MemoryRouter>,
    );

    expect(screen.getByLabelText('Уровень образования')).not.toBeVisible();
    await user.click(screen.getByRole('button', { name: 'Образование' }));
    expect(screen.getByLabelText('Уровень образования')).toBeVisible();
    unmount();

    render(<CatalogPage />);
    expect(screen.getByPlaceholderText('От 1 года')).not.toBeVisible();
    await user.click(screen.getByRole('button', { name: 'Дополнительно' }));
    expect(screen.getByPlaceholderText('От 1 года')).toBeInTheDocument();
  });
});
