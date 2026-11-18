import { FormEvent, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { connectToChat, type ChatMessage } from '../../shared/lib/chat/chat-client';

export function ChatPage() {
  const { matchId = '' } = useParams();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    try {
      const connection = connectToChat(matchId, (message) => setMessages((current) => [...current, message]), setError);
      setSocket(connection);
      return () => connection.close();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Не удалось открыть чат.');
      return undefined;
    }
  }, [matchId]);

  const submit = (event: FormEvent) => {
    event.preventDefault();
    const text = draft.trim();
    if (!text || !socket || socket.readyState !== WebSocket.OPEN) {
      if (!socket || socket.readyState !== WebSocket.OPEN) setError('Чат ещё подключается. Попробуйте через несколько секунд.');
      return;
    }
    socket.send(JSON.stringify({ type: 'message', text, client_message_id: crypto.randomUUID() }));
    setDraft('');
  };

  return (
    <section className="chat-page" aria-label="Чат мэтча">
      <div className="chat-page__header">
        <div>
          <p className="section-badge">Групповой чат</p>
          <h1>Чат мэтча #{matchId}</h1>
        </div>
        <span className={`chat-page__status ${socket?.readyState === WebSocket.OPEN ? 'chat-page__status--online' : ''}`}>
          {socket?.readyState === WebSocket.OPEN ? 'Подключено' : 'Подключение'}
        </span>
      </div>
      {error ? <p className="chat-page__error" role="alert">{error}</p> : null}
      <div className="chat-page__messages" aria-live="polite">
        {messages.length ? messages.map((message) => (
          <article className="chat-message" key={message.id}>
            <p>{message.text}</p>
            <time dateTime={message.sentAt}>{new Date(message.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</time>
          </article>
        )) : <p className="chat-page__empty">Сообщений пока нет. Напишите первое сообщение команде.</p>}
      </div>
      <form className="chat-page__form" onSubmit={submit}>
        <label className="visually-hidden" htmlFor="chat-message">Сообщение</label>
        <input id="chat-message" value={draft} maxLength={2000} onChange={(event) => setDraft(event.target.value)} placeholder="Напишите сообщение" />
        <button type="submit">Отправить</button>
      </form>
    </section>
  );
}
