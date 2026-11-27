# Matchmaker Frontend

React-клиент учебного проекта **Matchmaker** — сервиса поиска людей для команды, идеи, проекта или хакатона.

## Стек

React 18, TypeScript, Vite, React Router, Zod, Vitest и React Testing Library. Адаптивная верстка реализована средствами CSS/Flexbox.

Мобильный клиент находится отдельно в `ios/`: Kotlin Multiplatform, Compose Multiplatform и Ktor Client.

## Готовые пользовательские экраны

- вход;
- регистрация;
- подтверждение e-mail;
- ошибки e-mail, пароля, имени пользователя и сети;
- защищённые заготовки ленты, проектов, профиля, совпадений и чата.

Прототип: [Figma — Project Matchmaker](https://www.figma.com/design/PQ2HmEr99bpWbDCxgcnMbe/Project-Matchmaker?node-id=0-1).

## Подключение API

Контракт backend: [api.md](https://github.com/BartashevichEgor/matchmaker-backend/blob/main/core-api/docs/api.md). Интерактивная документация доступна у запущенного backend на `http://127.0.0.1:8000/docs`.

```powershell
# in the repository root
.\start.ps1
```

The script creates `web/.env` from `.env.example` on the first launch and
starts Vite on `http://127.0.0.1:5173`.  It uses Node.js 20+ from the normal
system installation; in this Codex workspace it can also use the bundled
runtime when dependencies are already installed.

Manual launch remains available:

```bash
cd web
copy .env.example .env
npm install
npm run dev
```

По умолчанию `VITE_API_BASE_URL=http://127.0.0.1:8000/api/v1`. Для доступа к серверу на другом компьютере укажите в `.env` опубликованный HTTPS-адрес API с суффиксом `/api/v1`, затем перезапустите Vite.

Подробности запуска и проверки находятся в [web/README.md](web/README.md).

## Мобильное приложение

Android и iOS используют общий интерфейс из `ios/shared`. Широкие экраны (от 600 dp) получают планшетные отступы, узкие — телефонную раскладку.

1. Откройте папку `ios` в Android Studio и дождитесь синхронизации Gradle.
2. Запустите конфигурацию `androidApp` на телефоне или эмуляторе.
3. Пробросьте API и чат в Android-эмулятор: `adb reverse tcp:8000 tcp:8000` и `adb reverse tcp:3000 tcp:3000`. Для физического телефона укажите адрес компьютера в `ios/shared/src/androidMain/kotlin/com/example/projectmatchmaker/network/ApiConfig.android.kt`.

Собрать APK можно командой из папки `ios`:

```bash
./gradlew :androidApp:assembleDebug
```

Переписки используют отдельный чат-сервис на порту `3000`; он должен быть запущен вместе с backend.
