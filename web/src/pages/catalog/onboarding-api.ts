import { apiRequest } from '../../shared/lib/api-client';

export type CatalogOnboarding = {
  mode: 'developer' | 'project';
  profile: { complete: boolean };
  project: { complete: boolean };
};

export function getCatalogOnboarding(signal?: AbortSignal): Promise<CatalogOnboarding> {
  return apiRequest<CatalogOnboarding>('/onboarding', { signal });
}
