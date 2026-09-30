import type { ReactNode } from 'react';

type AuthPageLayoutProps = {
  children: ReactNode;
};

export function AuthPageLayout({ children }: AuthPageLayoutProps) {
  return (
    <main className="auth-page-layout">
      <section className="auth-page-layout__visual" aria-label="Презентационная панель">
        <div className="auth-page-layout__visual-art" aria-hidden="true">
          <span />
          <span />
          <span />
        </div>
        <div className="auth-page-layout__visual-content">
          <p className="auth-page-layout__eyebrow">Matchmaker</p>
          <h1>Люди и проекты, которые подходят друг другу</h1>
          <p>Собирайте сильные команды и находите идеи для совместного развития.</p>
        </div>
      </section>

      <section className="auth-page-layout__form">
        <div className="section-card auth-page">{children}</div>
      </section>
    </main>
  );
}
