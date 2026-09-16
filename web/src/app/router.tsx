import { Route, Routes } from 'react-router-dom';
import { HomePage } from '../pages/home/HomePage';
import { LoginPage } from '../pages/login/LoginPage';
import { RegisterPage } from '../pages/register/RegisterPage';
import { NotFoundPage } from '../pages/not-found/NotFoundPage';
import { SectionPage } from '../shared/ui/SectionPage';

export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/feed" element={<SectionPage title="Лента" description="Здесь появится главная лента подбора." />} />
      <Route path="/projects" element={<SectionPage title="Проекты" description="Раздел с проектами находится в разработке." />} />
      <Route path="/profile" element={<SectionPage title="Профиль" description="Здесь будет профиль пользователя." />} />
      <Route path="/matches" element={<SectionPage title="Мэтчи" description="Список мэтчей будет доступен позже." />} />
      <Route path="/chat/:matchId" element={<SectionPage title="Чат" description="Чат-экран готовится к подключению." />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}