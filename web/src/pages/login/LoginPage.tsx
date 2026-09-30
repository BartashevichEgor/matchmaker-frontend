import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';
import { AuthForm } from '../../features/auth/ui/AuthForm';
import { resolveRedirectTarget, type RedirectState } from '../../features/auth/lib/resolve-redirect-target';

export function LoginPage() {
  const { user, isInitialized, authError, authErrorCode, clearAuthError, login } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  if (isInitialized && user) {
    return <Navigate to="/feed" replace />;
  }

  const redirectTarget = resolveRedirectTarget(location.state as RedirectState);

  const handleSubmit = async (input: { email: string; password: string }) => {
    await login(input);
    navigate(redirectTarget, { replace: true });
  };

  return (
    <main className="page-shell">
      <section className="section-card auth-page">
        <AuthForm
          mode="login"
          title="Войти в аккаунт"
          description="Авторизуйтесь, чтобы получить доступ к защищённым разделам."
          submitLabel="Войти"
          onSubmit={handleSubmit}
          externalError={authError}
          externalErrorCode={authErrorCode}
          onClearExternalError={clearAuthError}
        />
      </section>
    </main>
  );
}
