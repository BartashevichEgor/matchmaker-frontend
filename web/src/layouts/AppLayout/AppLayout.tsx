import { useNavigate, useLocation, Link, Outlet, type Location as RouterLocation } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';

type NavItem = {
  path: string;
  label: string;
  exact?: boolean;
};

const NAV_ITEMS: NavItem[] = [
  { path: '/feed', label: 'Лента', exact: true },
  { path: '/projects', label: 'Проекты', exact: true },
  { path: '/profile', label: 'Профиль', exact: true },
  { path: '/matches', label: 'Мэтчи', exact: true },
];

function isActive(location: RouterLocation, path: string, exact?: boolean): boolean {
  if (exact) {
    return location.pathname === path;
  }
  return location.pathname.startsWith(path);
}

export function AppLayout() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  return (
    <div className="app-layout">
      <header className="app-header">
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
            {/* Чат не в навигации — нужен реальный matchId */}
          </ul>
        </nav>
        <button className="logout-button" onClick={handleLogout}>
          Выход
        </button>
      </header>
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}