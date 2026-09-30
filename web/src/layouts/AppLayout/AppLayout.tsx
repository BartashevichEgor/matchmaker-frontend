import { useState } from 'react';
import { useLocation, Link, Outlet, type Location as RouterLocation } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';

type NavItem = {
  path: string;
  label: string;
  exact?: boolean;
};

const NAV_ITEMS: NavItem[] = [
  { path: '/feed', label: 'Feed', exact: true },
  { path: '/projects', label: 'Projects', exact: true },
  { path: '/profile', label: 'Profile', exact: true },
  { path: '/matches', label: 'Matches', exact: true },
];

function isActive(location: RouterLocation, path: string, exact?: boolean): boolean {
  if (exact) {
    return location.pathname === path;
  }
  return location.pathname.startsWith(path);
}

export function AppLayout() {
  const { logout, authError } = useAuth();
  const location = useLocation();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [logoutError, setLogoutError] = useState<string | null>(null);

  const handleLogout = async () => {
    if (isLoggingOut) {
      return;
    }

    setLogoutError(null);
    setIsLoggingOut(true);

    try {
      await logout();
    } catch {
      setLogoutError('Не удалось выйти из аккаунта. Попробуйте ещё раз.');
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <div className="app-layout">
      <header className="app-header">
        <div className="header-container">
          <Link to="/feed" className="app-logo">
            Matchmaker
          </Link>
          <nav className="app-nav">
            <ul className="nav-list">
              {NAV_ITEMS.map((item) => (
                <li key={item.path} className="nav-item">
                  <Link
                    to={item.path}
                    className={`nav-link${isActive(location, item.path, item.exact) ? ' nav-link--active' : ''}`}
                  >
                    {item.label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
          <button
            className="logout-button"
            type="button"
            onClick={() => void handleLogout()}
            disabled={isLoggingOut}
            aria-busy={isLoggingOut}
          >
            {isLoggingOut ? 'Выход...' : 'Exit'}
          </button>
        </div>
      </header>
      <main className="app-content">
        {logoutError || authError ? (
          <p className="auth-form__form-error" role="alert">
            {logoutError ?? authError}
          </p>
        ) : null}
        <Outlet />
      </main>
    </div>
  );
}
