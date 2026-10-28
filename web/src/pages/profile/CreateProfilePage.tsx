import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ProfileAvatar } from '../../entities/profile/ui/ProfileAvatar';
import { EDUCATION_LEVELS, getProfileDisplayName } from '../../entities/profile/model/profile.types';
import { Accordion } from '../../shared/ui/Accordion';
import { SelectField } from '../../shared/ui/SelectField';
import { useProfileDraft } from '../../features/profile-edit/model/use-profile-draft';
import { ProfileSkillsInput } from '../../features/profile-edit/ui/ProfileSkillsInput';

const ROLE_OPTIONS = [
  'UI/UX-дизайнер',
  'Frontend-разработчик',
  'Backend-разработчик',
  'Мобильный разработчик',
  'Менеджер проекта',
  'Аналитик',
];

const SKILL_OPTIONS = [
  'Figma',
  'UX',
  'UI',
  'TypeScript',
  'Python',
  'Django',
  'SQL',
  'Git',
  'Mermaid',
] as const;

export function CreateProfilePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [step, setStep] = useState(searchParams.get('step') === '2' ? 2 : 1);
  const [openSections, setOpenSections] = useState({ about: false, skills: false, education: false, courses: false });
  const { draft, skillInput, setSkillInput, updateDraft, addSkill, removeSkill } = useProfileDraft();
  const displayName = getProfileDisplayName(draft);

  return (
    <section className="create-profile-page" aria-labelledby="create-profile-title">
      <form className="create-profile-form" onSubmit={(event) => event.preventDefault()}>
        <h1 id="create-profile-title">Создание профиля</h1>
        <p className="create-profile-form__step">Шаг {step} из 2 · {step === 1 ? 'Основное' : 'Навыки и образование'}</p>
        <div className="create-profile-form__progress"><span className={step === 2 ? 'is-complete' : ''} /></div>

        {step === 1 ? (
          <div className="create-profile-form__body">
            <div className="create-profile-name-grid">
              <label className="create-profile-field">
                <span>Имя *</span>
                <input placeholder="Ваше имя" value={draft.firstName} onChange={(event) => updateDraft('firstName', event.target.value)} />
              </label>
              <label className="create-profile-field">
                <span>Фамилия *</span>
                <input placeholder="Ваша фамилия" value={draft.lastName} onChange={(event) => updateDraft('lastName', event.target.value)} />
              </label>
            </div>

            <label className="create-profile-field">
              <span>Роль в команде *</span>
              <SelectField value={draft.role} onChange={(event) => updateDraft('role', event.target.value)}>
                <option value="">Выберите роль</option>
                {ROLE_OPTIONS.map((role) => <option key={role} value={role}>{role}</option>)}
              </SelectField>
            </label>
            <p className="create-profile-role-help">Что означают роли?</p>

            <Accordion
              className="create-profile-accordion"
              title="Фото и о себе · необязательно"
              open={openSections.about}
              onToggle={() => setOpenSections((current) => ({ ...current, about: !current.about }))}
            >
              <button className="create-profile-photo" type="button" aria-label="Добавить фото">
                <ProfileAvatar name={displayName} className="create-profile-photo__avatar" />
                <span>
                  <strong>Добавить фото</strong>
                </span>
              </button>
              <label className="create-profile-field create-profile-photo-about">
                <span>О себе</span>
                <textarea placeholder="Расскажите о себе и своих проектах" value={draft.about} onChange={(event) => updateDraft('about', event.target.value)} />
              </label>
            </Accordion>

            <small className="create-profile-required">* Обязательные поля</small>
            <button className="button button-primary create-profile-submit" type="button" onClick={() => setStep(2)}>
              Далее: навыки и образование
            </button>
          </div>
        ) : (
          <div className="create-profile-form__body">
            <div className="create-profile-field">
              <span>Навыки *</span>
              <ProfileSkillsInput skills={draft.skills} value={skillInput} onChange={setSkillInput} onAdd={addSkill} onRemove={removeSkill} />
              <Accordion
                className="create-profile-skills-accordion"
                title="Выбрать навыки"
                open={openSections.skills}
                onToggle={() => setOpenSections((current) => ({ ...current, skills: !current.skills }))}
              >
                <div className="create-profile-skill-options" aria-label="Готовые навыки">
                  {SKILL_OPTIONS.map((skill) => (
                    <label className="create-profile-skill-option" key={skill}>
                      <input
                        type="checkbox"
                        checked={draft.skills.includes(skill)}
                        onChange={() => updateDraft('skills', draft.skills.includes(skill)
                          ? draft.skills.filter((currentSkill) => currentSkill !== skill)
                          : [...draft.skills, skill])}
                      />
                      <span>{skill}</span>
                    </label>
                  ))}
                </div>
              </Accordion>
            </div>

            <label className="create-profile-field">
              <span>Опыт работы</span>
              <input placeholder="Учебные и личные проекты" value={draft.experience} onChange={(event) => updateDraft('experience', event.target.value)} />
            </label>

            <Accordion
              className="create-profile-accordion"
              title="Образование"
              open={openSections.education}
              onToggle={() => setOpenSections((current) => ({ ...current, education: !current.education }))}
            >
              <p className="create-profile-section-help">Необязательно. Укажи уровень и место обучения.</p>
              <div className="create-profile-education-fields">
                <label className="create-profile-field">
                  <span>Уровень образования</span>
                  <SelectField value={draft.education} onChange={(event) => updateDraft('education', event.target.value)}>
                    <option value="">Выберите уровень</option>
                    {EDUCATION_LEVELS.map((level) => <option key={level} value={level}>{level}</option>)}
                  </SelectField>
                </label>
                <label className="create-profile-field">
                  <span>Учебное заведение</span>
                  <input placeholder="Например, БГУ" value={draft.educationInstitution} onChange={(event) => updateDraft('educationInstitution', event.target.value)} />
                </label>
                <label className="create-profile-field">
                  <span>Специальность / направление</span>
                  <input placeholder="Информатика" value={draft.educationSpecialty} onChange={(event) => updateDraft('educationSpecialty', event.target.value)} />
                </label>
                <label className="create-profile-field">
                  <span>Статус обучения</span>
                  <input placeholder="Учусь сейчас" value={draft.educationStatus} onChange={(event) => updateDraft('educationStatus', event.target.value)} />
                </label>
                <label className="create-profile-field">
                  <span>Год окончания или планируемый год</span>
                  <input placeholder="Например, 2028" value={draft.educationCompletionYear} onChange={(event) => updateDraft('educationCompletionYear', event.target.value)} />
                </label>
              </div>
            </Accordion>

            <Accordion
              className="create-profile-accordion"
              title="Курсы и сертификаты"
              open={openSections.courses}
              onToggle={() => setOpenSections((current) => ({ ...current, courses: !current.courses }))}
            >
              <p className="create-profile-section-help">Заполни, если проходил(а) дополнительное обучение.</p>
              <div className="create-profile-course-fields">
                <label className="create-profile-field">
                  <span>Название курса</span>
                  <input placeholder="Например, UX-исследования" value={draft.courseName} onChange={(event) => updateDraft('courseName', event.target.value)} />
                </label>
                <label className="create-profile-field">
                  <span>Школа / организатор</span>
                  <input placeholder="Например, онлайн-школа" value={draft.courseProvider} onChange={(event) => updateDraft('courseProvider', event.target.value)} />
                </label>
                <label className="create-profile-field">
                  <span>Год завершения курса</span>
                  <input placeholder="Например, 2026" value={draft.courseYear} onChange={(event) => updateDraft('courseYear', event.target.value)} />
                </label>
                <label className="create-profile-field">
                  <span>Ссылка на сертификат</span>
                  <input placeholder="Необязательно" value={draft.certificateUrl} onChange={(event) => updateDraft('certificateUrl', event.target.value)} />
                </label>
              </div>
            </Accordion>

            <small className="create-profile-required">Дополнительные разделы необязательны</small>
            <div className="create-profile-actions">
              <button className="create-profile-back" type="button" onClick={() => setStep(1)}>Назад</button>
              <button className="button button-primary create-profile-submit" type="button" onClick={() => navigate('/profile')}>
                Сохранить профиль
              </button>
            </div>
          </div>
        )}
      </form>
    </section>
  );
}
