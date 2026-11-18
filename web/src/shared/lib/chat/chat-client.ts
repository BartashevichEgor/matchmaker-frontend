type StoredTokens = { access_token: string };

export type ChatMessage = {
  id: string;
  text: string;
  sentAt: string;
};

const TOKEN_STORAGE_KEY = 'matchmaker:v1:tokens';
const CHAT_URL = import.meta.env.VITE_CHAT_WS_URL ?? 'ws://127.0.0.1:8010/ws';

function getAccessToken() {
  const raw = window.localStorage.getItem(TOKEN_STORAGE_KEY);
  if (!raw) throw new Error('Сначала войдите в аккаунт.');
  const tokens = JSON.parse(raw) as Partial<StoredTokens>;
  if (!tokens.access_token) throw new Error('Сессия устарела. Войдите повторно.');
  return tokens.access_token;
}

export function connectToChat(matchId: string, onMessage: (message: ChatMessage) => void, onError: (message: string) => void) {
  const numericMatchId = Number(matchId);
  if (!Number.isInteger(numericMatchId) || numericMatchId <= 0) throw new Error('Некорректный идентификатор мэтча.');

  const socket = new WebSocket(CHAT_URL);
  socket.addEventListener('open', () => {
    try {
      socket.send(JSON.stringify({ type: 'authenticate', access_token: getAccessToken(), match_id: numericMatchId }));
    } catch (error) {
      onError(error instanceof Error ? error.message : 'Не удалось авторизовать чат.');
      socket.close();
    }
  });
  socket.addEventListener('message', (event) => {
    try {
      const payload = JSON.parse(String(event.data)) as { type?: string; text?: string; sent_at?: string; code?: string; client_message_id?: string };
      if (payload.type === 'message' && typeof payload.text === 'string') {
        onMessage({ id: payload.client_message_id || crypto.randomUUID(), text: payload.text, sentAt: payload.sent_at ?? new Date().toISOString() });
      }
      if (payload.type === 'error') onError(payload.code === 'access_denied' ? 'Нет доступа к этому чату.' : 'Ошибка соединения с чатом.');
    } catch {
      onError('Сервер чата вернул некорректный ответ.');
    }
  });
  socket.addEventListener('error', () => onError('Не удалось подключиться к сервису чата.'));
  return socket;
}
