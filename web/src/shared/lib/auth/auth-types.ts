export type User = {
  id: string;
  email: string;
  createdAt: string;
};

export type CredentialsInput = {
  email: string;
  password: string;
};

export type RegisterFormInput = CredentialsInput & {
  confirmPassword: string;
};

export type AuthService = {
  register(input: CredentialsInput): Promise<User>;
  login(input: CredentialsInput): Promise<User>;
  logout(): Promise<void>;
  getCurrentUser(): Promise<User | null>;
};

export type AuthErrorCode = 'duplicate_email' | 'invalid_credentials' | 'storage_unavailable' | 'validation_error';

export class AuthError extends Error {
  code: AuthErrorCode;
  cause?: unknown;

  constructor(code: AuthErrorCode, message: string, cause?: unknown) {
    super(message);
    this.name = 'AuthError';
    this.code = code;
    this.cause = cause;
  }
}
