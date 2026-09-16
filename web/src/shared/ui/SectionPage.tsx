import { useParams } from 'react-router-dom';

type SectionPageProps = {
  title: string;
  description: string;
};

export function SectionPage({ title, description }: SectionPageProps) {
  const params = useParams();

  return (
    <main className="page-shell">
      <section className="section-card">
        <p className="section-badge">В разработке</p>
        <h1>{title}</h1>
        <p>{description}</p>
        {params.matchId ? (
          <p className="section-meta">matchId: {params.matchId}</p>
        ) : null}
      </section>
    </main>
  );
}