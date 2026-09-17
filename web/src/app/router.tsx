import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './providers/ProtectedRoute';
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
      <Route path="/feed" element={<ProtectedRoute><SectionPage title="Лента" description="Здесь появится главная лента подбора." /></ProtectedRoute>} />
      <Route path="/projects" element={<ProtectedRoute><SectionPage title="Проекты" description="Раздел с проектами находится в разработке." /></ProtectedRoute>} />
      <Route path="/profile" element={<ProtectedRoute><SectionPage title="Профиль" description="Здесь будет профиль пользователя." /></ProtectedRoute>} />
      <Route path="/matches" element={<ProtectedRoute><SectionPage title="Мэтчи" description="Список мэтчей будет доступен позже." /></ProtectedRoute>} />
      <Route path="/chat/:matchId" element={<ProtectedRoute><SectionPage title="Чат" description="Чат-экран готовится к подключению." /></ProtectedRoute>} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}