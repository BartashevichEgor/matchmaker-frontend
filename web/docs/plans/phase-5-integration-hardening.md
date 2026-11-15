# Web Phase 5 — Production integration and acceptance implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove remaining web mocks, standardize API behavior, integrate all phases and produce a production-ready web build.

**Architecture:** The web uses typed API clients from Phases 1–4, real backend/chat state and one centralized error/refresh layer. Production configuration comes from environment; no mock auth/cards/matches/chat are imported by application wiring.

**Tech Stack:** React, TypeScript, Vite, fetch, WebSocket, Vitest, TypeScript compiler.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`.

## Global Constraints

- Only standalone `frontend/web` source is modified.
- Production code must not import mock auth or hardcoded card/match/chat data.
- One refresh retry per request; offline/401/403/404/409/429 errors are typed and recoverable.
- Routes respect onboarding state and actor mode.
- Web must not expose access tokens in URLs, localStorage or logs.

## Review Focus

- Completed login routes to main; incomplete initial setup routes correctly.
- Every page has loading/error/empty states and no duplicate retry requests.
- Project creation action disappears after the single project exists.
- Match/chat pages cannot bypass backend authorization.
- Production build contains no mock imports or stale backend-monorepo paths.

---

### Task 1: Standardize web API and auth wiring

**Files:**
- Modify: `frontend/web/src/app/App.tsx`
- Modify: `frontend/web/src/shared/lib/auth/api-auth-service.ts`
- Create: `frontend/web/src/shared/lib/api/http-client.ts`
- Test: `frontend/web/src/app/providers/AuthProvider.test.tsx`

**Interfaces:**
- Consumes: Web Phases 1–4 clients and backend error/rate-limit contracts.
- Produces: one request layer with request IDs, credentials, timeout, one refresh retry and structured `ApiError`.

- [ ] Step 1: Add failing tests for offline, one refresh retry, refresh cleanup, 403/404/409/429 mapping and `Retry-After`.
- [ ] Step 2: Make App production wiring use only `apiAuthService`; retain mock service only in tests.
- [ ] Step 3: Add abort/timeout handling and safe request logging.
- [ ] Step 4: Verify no access/refresh token persistence outside approved memory/cookie behavior.
- [ ] Step 5: Run Vitest/TypeScript build and commit `refactor: standardize web API wiring`.

### Task 2: Complete routes and screens

**Files:**
- Modify: `frontend/web/src/app/router.tsx`
- Modify: `frontend/web/src/pages/catalog/CatalogPage.tsx`
- Modify: `frontend/web/src/pages/profile/ProfilePage.tsx`
- Modify: `frontend/web/src/pages/projects/ProjectsPage.tsx`
- Modify: `frontend/web/src/pages/matches/MatchesPage.tsx`
- Modify: `frontend/web/src/pages/notifications/NotificationsPage.tsx`
- Modify: `frontend/web/src/pages/chat/ChatPage.tsx`
- Test: `frontend/web/src/app/router.test.tsx`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`

**Interfaces:**
- Consumes: Web Phases 1–4 routes and API clients.
- Produces: complete `/feed`, `/projects`, `/profile`, `/matches`, `/notifications` and `/chat/:matchId` flows.

- [ ] Step 1: Add route tests for onboarding redirect, unauthorized redirect, empty catalog, match detail, notifications and chat.
- [ ] Step 2: Remove static cards, local favorites, local matches and chat placeholders from production paths.
- [ ] Step 3: Ensure mode gating and one-project manage state are consistent across navigation and refresh.
- [ ] Step 4: Add accessible keyboard/focus/aria behavior to card, match, notification and chat actions.
- [ ] Step 5: Run web tests and commit `feat: complete web product routes`.

### Task 3: Run web acceptance and handoff

**Files:**
- Modify: `frontend/web/README.md`
- Test: `frontend/web/src/app/router.test.tsx`
- Test: `frontend/web/src/app/providers/AuthProvider.test.tsx`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`

**Interfaces:**
- Consumes: Backend acceptance checklist and Web Phases 1–4.
- Produces: web release checklist and acceptance report inputs.

- [ ] Step 1: Run Vitest, TypeScript check and production build.
- [ ] Step 2: Run two-account flow: developer registration, project registration, Mailpit verification, setup, login-to-main behavior and optional second-card setup.
- [ ] Step 3: Verify one project per account, valid developer↔project match, notifications, chat and unread state.
- [ ] Step 4: Verify no developer↔developer/project↔project UI actions exist.
- [ ] Step 5: Search for mock imports, backend monorepo paths and token-bearing URLs.
- [ ] Step 6: Document environment variables and known skips; send results to backend/iOS owners and commit `docs: complete web acceptance handoff`.

## Exit Criteria

- Production web uses only real auth/catalog/matching/notification/chat APIs.
- Onboarding and mode behavior matches backend contracts.
- Web build/tests pass with no mock production paths.

## Handoff

Send build artifact version, API base URL requirements, test results and known skips to Backend Phase 5 and iOS Phase 5.

## Rollback

Keep new API clients behind the existing route boundary if a release check fails; do not re-enable production mocks or store access tokens persistently.
