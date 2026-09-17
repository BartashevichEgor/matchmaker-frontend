import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthProvider';

type ProtectedRouteProps = {
  children: ReactNode;
};

function LoadingScreen() {
  return (
    <main className="page-shell">
      <section className="section-card">
        <p className="section-badge">Проверка доступа</p>
        <h1>Загружаем сессию</h1>
        <p>Пожалуйста, подождите, пока приложение восстановит авторизацию.</p>
      </section>
    </main>
  );
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { user, isInitialized } = useAuth();
  const location = useLocation();

  if (!isInitialized) {
    return <LoadingScreen />;
  }

  if (!user) {
    return (
      <Navigate
        to="/login"
        replace
        state={{
          from: {
            pathname: location.pathname,
            search: location.search,
            hash: location.hash,
          },
        }}
      />
    );
  }

  return <>{children}</>;
}
