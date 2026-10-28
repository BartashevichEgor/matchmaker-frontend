export type ProfileDraft = {
  firstName: string;
  lastName: string;
  role: string;
  about: string;
  skills: string[];
  experience: string;
  education: string;
  educationInstitution: string;
  educationSpecialty: string;
  educationStatus: string;
  educationCompletionYear: string;
  courses: string;
  courseName: string;
  courseProvider: string;
  courseYear: string;
  certificateUrl: string;
  github: string;
  portfolio: string;
  telegram: string;
};

export const PROFILE_DRAFT_STORAGE_KEY = 'matchmaker:v1:profile-draft';

export const EDUCATION_LEVELS = [
  'Среднее общее',
  'Профессионально-техническое',
  'Среднее специальное / колледж',
  'Неоконченное высшее',
  'Высшее · бакалавриат',
  'Высшее · специалитет',
  'Магистратура',
  'Аспирантура / докторантура',
  'Предпочитаю не указывать',
] as const;

export const emptyProfileDraft: ProfileDraft = {
  firstName: '',
  lastName: '',
  role: '',
  about: '',
  skills: ['Figma', 'UX', 'Mermaid'],
  experience: '',
  education: '',
  educationInstitution: '',
  educationSpecialty: '',
  educationStatus: '',
  educationCompletionYear: '',
  courses: '',
  courseName: '',
  courseProvider: '',
  courseYear: '',
  certificateUrl: '',
  github: '',
  portfolio: '',
  telegram: '',
};

export function readProfileDraft(): ProfileDraft {
  if (typeof window === 'undefined') {
    return emptyProfileDraft;
  }

  try {
    const stored = window.localStorage.getItem(PROFILE_DRAFT_STORAGE_KEY);
    if (!stored) return emptyProfileDraft;

    const parsed = JSON.parse(stored) as Partial<ProfileDraft>;
    return {
      ...emptyProfileDraft,
      ...parsed,
      skills: Array.isArray(parsed.skills) ? parsed.skills.filter((skill): skill is string => typeof skill === 'string') : emptyProfileDraft.skills,
    };
  } catch {
    return emptyProfileDraft;
  }
}

export function saveProfileDraft(draft: ProfileDraft) {
  if (typeof window === 'undefined') return;

  window.localStorage.setItem(PROFILE_DRAFT_STORAGE_KEY, JSON.stringify(draft));
}

export function getProfileDisplayName(draft: Pick<ProfileDraft, 'firstName' | 'lastName'>) {
  return [draft.firstName.trim(), draft.lastName.trim()].filter(Boolean).join(' ');
}
