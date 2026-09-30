import { useId, useState, type FormEvent } from 'react';
import type { ZodIssue } from 'zod';
import type { AuthErrorCode, CredentialsInput } from '../../../shared/lib/auth/auth-types';
import { credentialsSchema, registerFormSchema } from '../../../shared/lib/auth/auth-schemas';

type AuthFormMode = 'login' | 'register';
type FieldName = 'email' | 'password' | 'confirmPassword';
type FieldErrors = Partial<Record<FieldName, string>>;

type AuthFormProps = {
  mode: AuthFormMode;
  title: string;
  description: string;
  submitLabel: string;
  onSubmit: (input: CredentialsInput) => Promise<void>;
  externalError?: string | null;
  externalErrorCode?: AuthErrorCode | null;
  onClearExternalError?: () => void;
};

function isFieldName(value: unknown): value is FieldName {
  return value === 'email' || value === 'password' || value === 'confirmPassword';
}

function getFieldErrors(issues: readonly ZodIssue[]): FieldErrors {
  return issues.reduce<FieldErrors>((errors, issue) => {
    const fieldName = issue.path[0];

    if (isFieldName(fieldName) && !errors[fieldName]) {
      errors[fieldName] = issue.message;
    }

    return errors;
  }, {});
}

function getExternalFieldErrors(
  error: string | null | undefined,
  errorCode: AuthErrorCode | null | undefined,
): FieldErrors {
  if (!error) {
    return {};
  }

  if (errorCode === 'duplicate_email') {
    return { email: error };
  }

  if (errorCode === 'invalid_credentials') {
    return { password: error };
  }

  return {};
}

export function AuthForm({
  mode,
  title,
  description,
  submitLabel,
  onSubmit,
  externalError,
  externalErrorCode,
  onClearExternalError,
}: AuthFormProps) {
  const emailId = useId();
  const passwordId = useId();
  const confirmPasswordId = useId();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const externalFieldErrors = getExternalFieldErrors(externalError, externalErrorCode);
  const visibleFieldErrors = { ...externalFieldErrors, ...fieldErrors };
  const invalidCredentials = externalErrorCode === 'invalid_credentials';
  const visibleFormError = formError ?? (Object.keys(externalFieldErrors).length === 0 ? externalError : null);

  const clearExternalError = () => {
    onClearExternalError?.();
    setFormError(null);
  };

  const handleFieldChange = (fieldName: FieldName, value: string) => {
    if (fieldName === 'email') {
      setEmail(value);
    } else if (fieldName === 'password') {
      setPassword(value);
    } else {
      setConfirmPassword(value);
    }

    setFieldErrors((currentErrors) => ({ ...currentErrors, [fieldName]: undefined }));
    clearExternalError();
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFieldErrors({});
    setFormError(null);
    clearExternalError();

    const baseInput: CredentialsInput = {
      email,
      password,
    };

    const validationResult =
      mode === 'register'
        ? registerFormSchema.safeParse({ ...baseInput, confirmPassword })
        : credentialsSchema.safeParse(baseInput);

    if (!validationResult.success) {
      setFieldErrors(getFieldErrors(validationResult.error.issues));
      return;
    }

    setIsSubmitting(true);

    try {
      await onSubmit(baseInput);
    } catch {
      // AuthProvider exposes the service error through externalError.
    } finally {
      setIsSubmitting(false);
    }
  };

  const emailError = visibleFieldErrors.email;
  const passwordError = visibleFieldErrors.password;
  const confirmPasswordError = visibleFieldErrors.confirmPassword;
  const emailHasError = Boolean(emailError || invalidCredentials);
  const passwordHasError = Boolean(passwordError || invalidCredentials);

  return (
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      <div className="auth-form__header">
        <p className="section-badge">{mode === 'login' ? 'Вход' : 'Регистрация'}</p>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>

      {visibleFormError ? (
        <p role="alert" className="auth-form__form-error">
          {visibleFormError}
        </p>
      ) : null}

      <div className="auth-form__field">
        <label htmlFor={emailId}>Email</label>
        <input
          id={emailId}
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => handleFieldChange('email', event.target.value)}
          placeholder="name@example.com"
          aria-invalid={emailHasError}
          aria-describedby={emailError ? `${emailId}-error` : undefined}
        />
        {emailError ? (
          <p id={`${emailId}-error`} className="auth-form__field-error" role="alert">
            {emailError}
          </p>
        ) : null}
      </div>

      <div className="auth-form__field">
        <label htmlFor={passwordId}>Пароль</label>
        <input
          id={passwordId}
          type="password"
          autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          value={password}
          onChange={(event) => handleFieldChange('password', event.target.value)}
          placeholder="Не менее 8 символов"
          aria-invalid={passwordHasError}
          aria-describedby={passwordError ? `${passwordId}-error` : undefined}
        />
        {passwordError ? (
          <p id={`${passwordId}-error`} className="auth-form__field-error" role="alert">
            {passwordError}
          </p>
        ) : null}
      </div>

      {mode === 'register' ? (
        <div className="auth-form__field">
          <label htmlFor={confirmPasswordId}>Подтвердите пароль</label>
          <input
            id={confirmPasswordId}
            type="password"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(event) => handleFieldChange('confirmPassword', event.target.value)}
            placeholder="Повторите пароль"
            aria-invalid={Boolean(confirmPasswordError)}
            aria-describedby={confirmPasswordError ? `${confirmPasswordId}-error` : undefined}
          />
          {confirmPasswordError ? (
            <p id={`${confirmPasswordId}-error`} className="auth-form__field-error" role="alert">
              {confirmPasswordError}
            </p>
          ) : null}
        </div>
      ) : null}

      <button className="button button-primary auth-form__submit" type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Отправка...' : submitLabel}
      </button>
    </form>
  );
}
