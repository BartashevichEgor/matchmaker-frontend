import { useEffect, useState } from 'react';
import { Accordion } from '../../shared/ui/Accordion';
import { getProjects, type CatalogProject } from './projects-api';
import { getCatalogOnboarding, type CatalogOnboarding } from './onboarding-api';

type CatalogMode = 'specialists' | 'projects';

const ROLE_FILTERS = [
  'UI/UX-дизайнер',
  'Frontend-разработчик',
  'Backend-разработчик',
  'Мобильный разработчик',
  'Менеджер проекта',
  'Аналитик',
];

const SKILL_FILTERS = [
  'Figma',
  'UX',
  'UI',
  'Python',
  'Django',
  'SQL',
  'React',
  'TypeScript',
  'Mermaid',
];

function CrossIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="catalog-action-icon">
      <path d="m7 7 10 10M17 7 7 17" />
    </svg>
  );
}

function HeartIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="catalog-action-icon">
      <path d="M20.5 8.8c0 5.2-8.5 10.2-8.5 10.2S3.5 14 3.5 8.8A4.4 4.4 0 0 1 12 7.1a4.4 4.4 0 0 1 8.5 1.7Z" />
    </svg>
  );
}

function BookmarkIcon({ filled = false }: { filled?: boolean }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="catalog-bookmark-icon">
      <path className={filled ? 'is-filled' : ''} d="M7 4.75A1.75 1.75 0 0 1 8.75 3h6.5A1.75 1.75 0 0 1 17 4.75V21l-5-3-5 3V4.75Z" />
    </svg>
  );
}

function FilterIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="catalog-filter-icon">
      <path d="M4 7h16M4 17h16M9 7a2 2 0 1 0 0 0.01M15 17a2 2 0 1 0 0 0.01" />
    </svg>
  );
}

function FilterField({ label, placeholder }: { label: string; placeholder: string }) {
  return (
    <label className="catalog-filter__field">
      <span>{label}</span>
      <input placeholder={placeholder} />
    </label>
  );
}

function MultiSelectFilter({
  title,
  options,
  selected,
  open,
  onToggle,
  onChange,
}: {
  title: string;
  options: string[];
  selected: string[];
  open: boolean;
  onToggle: () => void;
  onChange: (value: string) => void;
}) {
  return (
    <Accordion
      className="catalog-filter__collapse"
      title={`${title}${selected.length ? ` · ${selected.length}` : ''}`}
      open={open}
      onToggle={onToggle}
    >
      <div className="catalog-filter__options" role="group" aria-label={title}>
        {options.map((option) => (
          <label className="catalog-filter__option" key={option}>
            <input
              type="checkbox"
              checked={selected.includes(option)}
              onChange={() => onChange(option)}
            />
            <span>{option}</span>
          </label>
        ))}
      </div>
    </Accordion>
  );
}

export function CatalogPage() {
  const [selectedMode, setSelectedMode] = useState<CatalogMode>('specialists');
  const [onboarding, setOnboarding] = useState<CatalogOnboarding | null>(null);
  const [onboardingState, setOnboardingState] = useState<'loading' | 'ready' | 'error'>('loading');
  const [onboardingError, setOnboardingError] = useState('');
  const [onboardingRequest, setOnboardingRequest] = useState(0);
  const [openFilters, setOpenFilters] = useState({ roles: false, skills: false, advanced: false });
  const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
  const [selectedSkills, setSelectedSkills] = useState<string[]>([]);
  const [isMobileFiltersOpen, setIsMobileFiltersOpen] = useState(false);
  const [projects, setProjects] = useState<CatalogProject[]>([]);
  const [projectsState, setProjectsState] = useState<'idle' | 'loading' | 'ready' | 'error'>('idle');
  const [projectsError, setProjectsError] = useState('');
  const [projectsRequest, setProjectsRequest] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setOnboardingState('loading');
    setOnboardingError('');

    getCatalogOnboarding(controller.signal)
      .then((nextOnboarding) => {
        setOnboarding(nextOnboarding);
        setSelectedMode(nextOnboarding.mode === 'developer' ? 'projects' : 'specialists');
        setOnboardingState('ready');
      })
      .catch((error: unknown) => {
        if (controller.signal.aborted) return;
        setOnboardingState('error');
        setOnboardingError(error instanceof Error ? error.message : 'Не удалось загрузить состояние карточек.');
      });

    return () => controller.abort();
  }, [onboardingRequest]);

  const hasProfile = onboarding?.profile.complete ?? false;
  const hasProject = onboarding?.project.complete ?? false;
  const showSwitcher = hasProfile && hasProject;
  const mode: CatalogMode | null = showSwitcher
    ? selectedMode
    : hasProfile
      ? 'projects'
      : hasProject
        ? 'specialists'
        : null;

  useEffect(() => {
    if (mode !== 'projects') return;

    const controller = new AbortController();
    setProjectsState('loading');
    setProjectsError('');

    getProjects(controller.signal)
      .then((nextProjects) => {
        setProjects(nextProjects);
        setProjectsState('ready');
      })
      .catch((error: unknown) => {
        if (controller.signal.aborted) return;
        setProjectsState('error');
        setProjectsError(error instanceof Error ? error.message : 'Не удалось загрузить проекты.');
      });

    return () => controller.abort();
  }, [mode, projectsRequest]);

  const toggleSelected = (value: string, selected: string[], setSelected: (next: string[]) => void) => {
    setSelected(selected.includes(value) ? selected.filter((item) => item !== value) : [...selected, value]);
  };

  const matchesFilters = (role: string, skills: string[]) => {
    const roleMatches = selectedRoles.length === 0 || selectedRoles.includes(role);
    const skillMatches = selectedSkills.length === 0 || selectedSkills.some((skill) => skills.includes(skill));
    return roleMatches && skillMatches;
  };

  const hasMatchingSpecialist = matchesFilters('Backend-разработчик', ['Python', 'Django', 'SQL']);

  const filterControls = (
    <>
      <MultiSelectFilter
        title="Роли"
        options={ROLE_FILTERS}
        selected={selectedRoles}
        open={openFilters.roles}
        onToggle={() => setOpenFilters((current) => ({ ...current, roles: !current.roles }))}
        onChange={(value) => toggleSelected(value, selectedRoles, setSelectedRoles)}
      />
      <MultiSelectFilter
        title="Навыки"
        options={SKILL_FILTERS}
        selected={selectedSkills}
        open={openFilters.skills}
        onToggle={() => setOpenFilters((current) => ({ ...current, skills: !current.skills }))}
        onChange={(value) => toggleSelected(value, selectedSkills, setSelectedSkills)}
      />
      <Accordion
        className="catalog-filter__collapse"
        title="Дополнительно"
        open={openFilters.advanced}
        onToggle={() => setOpenFilters((current) => ({ ...current, advanced: !current.advanced }))}
      >
        <div className="catalog-filter__advanced">
          <FilterField label="Опыт" placeholder="От 1 года" />
          <FilterField label="Формат работы" placeholder="Удалённо или гибридно" />
        </div>
      </Accordion>
    </>
  );

  return (
    <section className="catalog-page" aria-labelledby="catalog-title">
      <div className="catalog-page__heading">
        <h1 id="catalog-title" aria-label="Лента — Каталог">Каталог</h1>
        <button
          className="catalog-page__filter-icon"
          type="button"
          aria-label="Открыть фильтры"
          aria-expanded={isMobileFiltersOpen}
          onClick={() => setIsMobileFiltersOpen((isOpen) => !isOpen)}
        >
          <FilterIcon />
        </button>
      </div>

      <div className="catalog-layout">
        <aside className={`catalog-filter${isMobileFiltersOpen ? ' catalog-filter--mobile-open' : ''}`} aria-label="Фильтры каталога">
          <h2>Фильтры</h2>
          {filterControls}
          <button className="button button-primary catalog-filter__submit" type="button">
            Применить
          </button>
        </aside>

        <div className="catalog-results">
          <h2>Подходят тебе</h2>
          {onboardingState === 'loading' && <p className="catalog-results__empty" role="status">Загружаем каталог…</p>}
          {onboardingState === 'error' && (
            <div className="catalog-results__empty" role="alert">
              <p>Не удалось определить доступный каталог: {onboardingError}</p>
              <button className="button button-primary" type="button" onClick={() => setOnboardingRequest((request) => request + 1)}>
                Повторить
              </button>
            </div>
          )}
          {onboardingState === 'ready' && mode === null && (
            <p className="catalog-results__empty" role="status">Заполните профиль специалиста или карточку проекта, чтобы открыть каталог.</p>
          )}
          {onboardingState === 'ready' && showSwitcher && (
            <div className="catalog-switcher" role="tablist" aria-label="Тип каталога">
              <button
                className={mode === 'projects' ? 'catalog-switcher__tab catalog-switcher__tab--active' : 'catalog-switcher__tab'}
                type="button"
                role="tab"
                aria-selected={mode === 'projects'}
                onClick={() => setSelectedMode('projects')}
              >
                Проекты
              </button>
              <button
                className={mode === 'specialists' ? 'catalog-switcher__tab catalog-switcher__tab--active' : 'catalog-switcher__tab'}
                type="button"
                role="tab"
                aria-selected={mode === 'specialists'}
                onClick={() => setSelectedMode('specialists')}
              >
                Специалисты
              </button>
            </div>
          )}

          {onboardingState === 'ready' && mode === 'specialists' && (hasMatchingSpecialist ? <SpecialistCard /> : (
            <p className="catalog-results__empty">По выбранным фильтрам ничего не найдено.</p>
          ))}

          {onboardingState === 'ready' && mode === 'projects' && projectsState === 'loading' && (
            <p className="catalog-results__empty" role="status">Загружаем проекты…</p>
          )}

          {onboardingState === 'ready' && mode === 'projects' && projectsState === 'error' && (
            <div className="catalog-results__empty" role="alert">
              <p>Не удалось загрузить каталог: {projectsError}</p>
              <button className="button button-primary" type="button" onClick={() => setProjectsRequest((request) => request + 1)}>
                Повторить
              </button>
            </div>
          )}

          {onboardingState === 'ready' && mode === 'projects' && projectsState === 'ready' && projects.length === 0 && (
            <p className="catalog-results__empty">В каталоге пока нет проектов.</p>
          )}

          {onboardingState === 'ready' && mode === 'projects' && projectsState === 'ready' && projects.map((project) => (
            <ProjectCard key={project.id} project={project} />
          ))}
        </div>
      </div>
    </section>
  );
}

function SpecialistCard() {
  const [isExpanded, setIsExpanded] = useState(false);
  const [isSaved, setIsSaved] = useState(false);

  return (
    <article className="catalog-card catalog-card--specialist">
      <div className="catalog-card__cover catalog-card__cover--portrait">
        <span className="catalog-card__match">87% совпадение</span>
        <span className="catalog-card__initials">АК</span>
      </div>
      <div className="catalog-card__title-row">
        <h3>Алексей Ковалёв</h3>
        <button className={`catalog-card__bookmark${isSaved ? ' is-saved' : ''}`} type="button" aria-label={isSaved ? 'Убрать из избранного' : 'Сохранить в избранное'} aria-pressed={isSaved} onClick={() => setIsSaved((saved) => !saved)}>
          <BookmarkIcon filled={isSaved} />
        </button>
      </div>
      <p className="catalog-card__role">Backend-разработчик</p>
      <div className="catalog-card__chips">
        <span>Python</span><span>Django</span><span>SQL</span>
      </div>
      <p className="catalog-card__description">Люблю чистый код. Ищу команду для полезного проекта.</p>
      <Accordion
        className="catalog-card__accordion"
        title="О себе"
        open={isExpanded}
        onToggle={() => setIsExpanded((isOpen) => !isOpen)}
      >
        <div className="catalog-card__accordion-details">
          <p><strong>Опыт:</strong> 3 года в backend-разработке и командных проектах.</p>
          <p><strong>GitHub:</strong> github.com/alexey-k</p>
        </div>
      </Accordion>
      <CatalogActions />
    </article>
  );
}

function ProjectCard({ project }: { project: CatalogProject }) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [isSaved, setIsSaved] = useState(false);

  return (
    <article className="catalog-card catalog-card--project">
      <div className="catalog-card__cover catalog-card__cover--project">
        <strong>{project.title}</strong>
        <span>{project.status === 'open' ? 'Открыт для участников' : `Статус: ${project.status}`}</span>
      </div>
      <div className="catalog-card__title-row">
        <h3>{project.title}</h3>
        <button className={`catalog-card__bookmark${isSaved ? ' is-saved' : ''}`} type="button" aria-label={isSaved ? 'Убрать из избранного' : 'Сохранить в избранное'} aria-pressed={isSaved} onClick={() => setIsSaved((saved) => !saved)}>
          <BookmarkIcon filled={isSaved} />
        </button>
      </div>
      <p className="catalog-card__role">{project.role_ids.length ? `Ищем специалистов: ${project.role_ids.map((roleId) => `роль #${roleId}`).join(', ')}` : 'Ищем участников в команду'}</p>
      <div className="catalog-card__chips">
        {project.skills.length ? project.skills.map((skill) => <span key={skill.skill_id}>Навык #{skill.skill_id}</span>) : <span>Навыки не указаны</span>}
      </div>
      <p className="catalog-card__description">{project.description || 'Описание проекта пока не добавлено.'}</p>
      <Accordion
        className="catalog-card__accordion"
        title="Описание проекта"
        open={isExpanded}
        onToggle={() => setIsExpanded((isOpen) => !isOpen)}
      >
        <div className="catalog-card__accordion-details">
          <p><strong>Статус:</strong> {project.status}</p>
          <p><strong>Дата создания:</strong> {new Date(project.created_at).toLocaleDateString('ru-RU')}</p>
        </div>
      </Accordion>
      <CatalogActions />
    </article>
  );
}

function CatalogActions() {
  return (
    <div className="catalog-card__actions">
      <button className="catalog-card__action catalog-card__action--reject" type="button" aria-label="Пропустить">
        <CrossIcon />
      </button>
      <button className="catalog-card__action catalog-card__action--like" type="button" aria-label="Добавить в избранное">
        <HeartIcon />
      </button>
    </div>
  );
}
