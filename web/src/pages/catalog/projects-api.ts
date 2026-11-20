import { apiRequest } from '../../shared/lib/api-client';

export type ProjectSkill = {
  skill_id: number;
  weight: number;
};

export type CatalogProject = {
  id: number;
  owner_id: number;
  title: string;
  description: string;
  status: string;
  created_at: string;
  skills: ProjectSkill[];
  role_ids: number[];
};

export function getProjects(signal?: AbortSignal): Promise<CatalogProject[]> {
  return apiRequest<CatalogProject[]>('/projects', { signal });
}
