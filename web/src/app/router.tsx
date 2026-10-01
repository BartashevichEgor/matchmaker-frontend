import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './providers/ProtectedRoute';
import { AppLayout } from '../layouts/AppLayout/AppLayout';
import { HomePage } from '../pages/home/HomePage';
import { LoginPage } from '../pages/login/LoginPage';
import { RegisterPage } from '../pages/register/RegisterPage';
import { NotFoundPage } from '../pages/not-found/NotFoundPage';
import { SectionPlaceholder } from '../shared/ui/SectionPlaceholder';

export function AppRouter() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      
      {/* All authenticated pages share one layout and one header. */}
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/feed">
          <Route
            index
            element={<SectionPlaceholder title="Лента" description="Здесь появится главная лента подбора." />}
          />
        </Route>

        <Route path="/projects">
          <Route
            index
            element={<SectionPlaceholder title="Проекты" description="Раздел с проектами находится в разработке." />}
          />
        </Route>

        <Route path="/profile">
          <Route
            index
            element={<SectionPlaceholder title="Профиль" description="Здесь будет профиль пользователя." />}
          />
        </Route>

        <Route path="/matches">
          <Route
            index
            element={<SectionPlaceholder title="Мэтчи" description="Список мэтчей будет доступен позже." />}
          />
        </Route>

        <Route path="/chat/:matchId">
          <Route
            index
            element={<SectionPlaceholder title="Чат" description="Чат-экран готовится к подключению." matchIdFromParams />}
          />
        </Route>
      </Route>
      
      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
