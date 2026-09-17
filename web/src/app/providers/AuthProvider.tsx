import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { CredentialsInput, User } from '../../shared/lib/auth/auth-types';
import { AuthError } from '../../shared/lib/auth/auth-types';
import { mockAuthService } from '../../shared/lib/auth/mock-auth-service';

type AuthContextValue = {
  user: User | null;
  isInitialized: boolean;
  authError: string | null;
  login: (input: CredentialsInput) => Promise<User>;
  register: (input: CredentialsInput) => Promise<User>;
  logout: () => Promise<void>;
  clearAuthError: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function getErrorMessage(error: unknown) {
  if (error instanceof AuthError) {
    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'Неизвестная ошибка авторизации';
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    const initializeAuth = async () => {
      try {
        const currentUser = await mockAuthService.getCurrentUser();

        if (!active) {
          return;
        }

        setUser(currentUser);
        setAuthError(null);
      } catch (error) {
        if (!active) {
          return;
        }

        setUser(null);
        setAuthError(getErrorMessage(error));
      } finally {
        if (active) {
          setIsInitialized(true);
        }
      }
    };

    void initializeAuth();

    return () => {
      active = false;
    };
  }, []);

  const contextValue = useMemo<AuthContextValue>(
    () => ({
      user,
      isInitialized,
      authError,
      clearAuthError: () => setAuthError(null),
      login: async (input: CredentialsInput) => {
        setAuthError(null);

        try {
          const nextUser = await mockAuthService.login(input);
          setUser(nextUser);
          return nextUser;
        } catch (error) {
          setAuthError(getErrorMessage(error));
          throw error;
        }
      },
      register: async (input: CredentialsInput) => {
        setAuthError(null);

        try {
          const nextUser = await mockAuthService.register(input);
          setUser(nextUser);
          return nextUser;
        } catch (error) {
          setAuthError(getErrorMessage(error));
          throw error;
        }
      },
      logout: async () => {
        setAuthError(null);

        try {
          await mockAuthService.logout();
          setUser(null);
        } catch (error) {
          setAuthError(getErrorMessage(error));
          throw error;
        }
      },
    }),
    [authError, isInitialized, user],
  );

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}
