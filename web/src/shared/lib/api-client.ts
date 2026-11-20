import { API_BASE_URL, getApiAccessToken } from './auth/api-auth-service';

type ApiErrorBody = { detail?: string };

export class ApiClientError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiClientError';
  }
}

/** Minimal authenticated client shared by feature-level API modules. */
export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const accessToken = getApiAccessToken();
  if (!accessToken) {
    throw new ApiClientError(401, 'Войдите в аккаунт, чтобы загрузить данные.');
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        Accept: 'application/json',
        Authorization: `Bearer ${accessToken}`,
        ...init.headers,
      },
    });
  } catch (error) {
    throw new ApiClientError(0, error instanceof Error ? error.message : 'Не удалось подключиться к серверу.');
  }

  const body = await response.json().catch(() => ({})) as ApiErrorBody & T;
  if (!response.ok) {
    throw new ApiClientError(response.status, body.detail ?? 'Не удалось загрузить данные.');
  }

  return body as T;
}
