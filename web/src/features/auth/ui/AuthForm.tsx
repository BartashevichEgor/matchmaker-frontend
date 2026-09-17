import { useId, useState, type FormEvent } from 'react';
import type { CredentialsInput, RegisterFormInput } from '../../../shared/lib/auth/auth-types';
import { credentialsSchema, registerFormSchema } from '../../../shared/lib/auth/auth-schemas';

type AuthFormMode = 'login' | 'register';

type AuthFormProps = {
  mode: AuthFormMode;
  title: string;
  description: string;
  submitLabel: string;
  onSubmit: (input: CredentialsInput) => Promise<void>;
  externalError?: string | null;
};

export function AuthForm({ mode, title, description, submitLabel, onSubmit, externalError }: AuthFormProps) {
  const emailId = useId();
  const passwordId = useId();
  const confirmPasswordId = useId();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [localError, setLocalError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLocalError(null);

    const baseInput = {
      email,
      password,
    };

    const validationResult =
      mode === 'register'
        ? registerFormSchema.safeParse({ ...baseInput, confirmPassword })
        : credentialsSchema.safeParse(baseInput);

    if (!validationResult.success) {
      setLocalError(validationResult.error.issues[0]?.message ?? 'Проверьте данные формы');
      return;
    }

    setIsSubmitting(true);

    try {
      await onSubmit(baseInput);
    } catch {
      // Error state is handled by the provider and displayed above the form.
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      <div className="auth-form__header">
        <p className="section-badge">{mode === 'login' ? 'Вход' : 'Регистрация'}</p>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>

      {(externalError || localError) ? (
        <div role="alert" className="auth-form__alert">
          {externalError ?? localError}
        </div>
      ) : null}

      <label className="auth-form__field" htmlFor={emailId}>
        <span>Email</span>
        <input
          id={emailId}
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="name@example.com"
        />
      </label>

      <label className="auth-form__field" htmlFor={passwordId}>
        <span>Пароль</span>
        <input
          id={passwordId}
          type="password"
          autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Не менее 8 символов"
        />
      </label>

      {mode === 'register' ? (
        <label className="auth-form__field" htmlFor={confirmPasswordId}>
          <span>Подтвердите пароль</span>
          <input
            id={confirmPasswordId}
            type="password"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            placeholder="Повторите пароль"
          />
        </label>
      ) : null}

      <button className="button button-primary auth-form__submit" type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Отправка...' : submitLabel}
      </button>
    </form>
  );
}
