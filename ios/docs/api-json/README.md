# JSON-контракт мобильного клиента

Примеры в этой папке сверены с Pydantic-моделями backend на коммите `ca52e4f`.
Базовый адрес Android-приложения: `http://127.0.0.1:8000/api/v1`.
Перед запуском локальной сборки выполните `adb reverse tcp:8000 tcp:8000`,
чтобы перенаправить порт эмулятора или USB-устройства на backend компьютера.

## Авторизация

| Операция | Метод и путь | Запрос | Ответ |
|---|---|---|---|
| Регистрация | `POST /api/v1/auth/register` | `register-request.json` | `register-response.json`, HTTP 201 |
| Подтверждение почты | `POST /api/v1/auth/verify-email` | `verify-email-request.json` | `{ "message": "..." }` |
| Отправить код повторно | `POST /api/v1/auth/resend-verification` | `resend-verification-request.json` | `{ "message": "..." }` |
| Вход | `POST /api/v1/auth/login` | `login-request.json` | `token-response.json` |
| Обновление токенов | `POST /api/v1/auth/refresh` | `refresh-request.json` | `token-response.json` |

После регистрации пользователь вводит шестизначный код из письма. Только после подтверждения почты выполняется вход. Access-токен действует 15 минут, refresh-токен — 7 дней.

## Профиль

| Операция | Метод и путь | Тело/ответ |
|---|---|---|
| Получить профиль | `GET /api/v1/profiles/me` | `profile-response.json` |
| Изменить профиль | `PATCH /api/v1/profiles/me` | `profile-request.json` |
| Заменить навыки | `PUT /api/v1/profiles/me/skills` | `profile-skills-request.json` |
| Получить навыки | `GET /api/v1/skills` | массив `{ "id": 1, "name": "Kotlin" }` |
| Получить роли | `GET /api/v1/roles` | массив `{ "id": 3, "name": "iOS-разработчик" }` |

`role_id` и `skill_id` берутся из справочников backend. Уровень навыка `level` — целое число от 1 до 5.

## Общие правила

1. Запросы отправляются с `Content-Type: application/json`.
2. Защищённые запросы содержат `Authorization: Bearer <access_token>`.
3. Названия JSON-полей записаны в `snake_case` и должны совпадать с backend.
4. Даты имеют формат `YYYY-MM-DD`, время — ISO 8601.
5. Обычная ошибка: `{ "detail": "Текст ошибки" }`.
6. Ошибка валидации HTTP 422 содержит массив объектов в поле `detail`.
7. Пароль при регистрации содержит от 8 до 128 символов; `username` — от 1 до 50.

Полное описание backend: `C:/Users/ASUS/AndroidStudioProjects/matchmaker-backend/core-api/docs/api.md`. Интерактивная документация доступна на `/docs`, когда backend запущен.
