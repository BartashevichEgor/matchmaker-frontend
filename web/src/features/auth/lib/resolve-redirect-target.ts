export type RedirectState = {
  from?: {
    pathname?: unknown;
    search?: unknown;
    hash?: unknown;
  } | null;
} | null | undefined;

export function resolveRedirectTarget(state: RedirectState, fallback = '/feed') {
  const from = state?.from;

  if (!from || typeof from.pathname !== 'string' || !isInternalPathname(from.pathname)) {
    return fallback;
  }

  const search = typeof from.search === 'string' ? from.search : '';
  const hash = typeof from.hash === 'string' ? from.hash : '';

  return `${from.pathname}${search}${hash}`;
}

function isInternalPathname(pathname: string) {
  if (!pathname.startsWith('/')) {
    return false;
  }

  try {
    const applicationOrigin = 'https://matchmaker.local';
    return new URL(pathname, applicationOrigin).origin === applicationOrigin;
  } catch {
    return false;
  }
}