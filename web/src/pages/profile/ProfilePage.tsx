import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../app/providers/AuthProvider';
import { getProfileDisplayName, readProfileDraft } from '../../entities/profile/model/profile.types';
import { ProfileAvatar } from '../../entities/profile/ui/ProfileAvatar';
import { Accordion } from '../../shared/ui/Accordion';

function BookmarkIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="profile-bookmark-icon">
      <path d="M7 4.75A1.75 1.75 0 0 1 8.75 3h6.5A1.75 1.75 0 0 1 17 4.75V21l-5-3-5 3V4.75Z" />
    </svg>
  );
}

export function ProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const draft = readProfileDraft();
  const profileName = getProfileDisplayName(draft) || 'Вероника Соболь';
  const username = user?.username ?? 'veronika';
  const role = draft.role || 'UI/UX-дизайнер';
  const about = draft.about || 'Создаю понятные интерфейсы. Ищу команду для полезного проекта.';
  const skills = draft.skills.length ? draft.skills : ['Figma', 'UX', 'Mermaid'];
  const education = draft.education || 'Высшее · бакалавриат';
  const educationDetails = [
    education,
    draft.educationInstitution,
    draft.educationSpecialty,
    draft.educationStatus,
    draft.educationCompletionYear ? `до ${draft.educationCompletionYear}` : '',
  ].filter(Boolean).join(' · ');
  const courses = [draft.courseName, draft.courseProvider, draft.courseYear]
    .filter(Boolean)
    .join(' · ') || draft.courses || 'Курс по UX-исследованиям';
  const [openSections, setOpenSections] = useState({ education: false, courses: false });
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [logoutError, setLogoutError] = useState<string | null>(null);

  const handleLogout = async () => {
    if (isLoggingOut) return;

    setLogoutError(null);
    setIsLoggingOut(true);

    try {
      await logout();
      navigate('/register', { replace: true });
    } catch {
      setLogoutError('Не удалось выйти из аккаунта. Попробуйте ещё раз.');
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <section className="profile-page" aria-labelledby="profile-title">
      <h1 id="profile-title">Мой профиль</h1>
      <article className="profile-card">
        <div className="profile-card__identity">
          <ProfileAvatar name={profileName} className="profile-card__avatar" />
          <div>
            <h2>{profileName}</h2>
            <p className="profile-card__username">@{username}</p>
            <p className="profile-card__role">{role}</p>
          </div>
        </div>

        <Link className="profile-favorite" to="/projects">
          <BookmarkIcon />
          <span><strong>Избранное</strong><small>Сохранённые анкеты и проекты</small></span>
        </Link>
        <Link className="button button-secondary profile-card__edit" to="/profile/create?step=1">
          Редактировать профиль
        </Link>

        <div className="profile-details">
          <section>
            <h2>О себе</h2>
            <p>{about}</p>
          </section>
          <section>
            <h2>Навыки</h2>
            <div className="profile-chips">{skills.map((skill) => <span key={skill}>{skill}</span>)}</div>
          </section>
          <Accordion
            className="profile-accordion"
            title="Образование и курсы"
            open={openSections.education || openSections.courses}
            onToggle={() => setOpenSections((current) => ({ ...current, education: !(current.education || current.courses), courses: false }))}
          >
            <div className="profile-accordion__details">
              <div><h3>Образование</h3><p>{educationDetails}</p></div>
              <div><h3>Курсы и сертификаты</h3><p>{courses}</p></div>
            </div>
          </Accordion>
        </div>

        <button
          className="button button-secondary profile-card__logout"
          type="button"
          onClick={() => void handleLogout()}
          disabled={isLoggingOut}
          aria-busy={isLoggingOut}
        >
          {isLoggingOut ? 'Выход...' : 'Выйти из профиля'}
        </button>
        {logoutError ? <p className="profile-card__error" role="alert">{logoutError}</p> : null}
      </article>
    </section>
  );
}
