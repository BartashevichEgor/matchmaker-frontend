# iOS Phase 5 — Production integration and acceptance implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove remaining mobile mocks, standardize API behavior and verify the complete production-oriented client flow.

**Architecture:** The shared KMP layer owns typed API/repository state; Android/iOS platform code owns secure storage and platform transport. All screens consume real backend/chat state and share onboarding/mode rules with Web.

**Tech Stack:** Kotlin Multiplatform, Compose, Ktor, WebSocket, kotlinx.serialization, commonTest, Android/iOS compilation.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`.

## Global Constraints

- Only `frontend/ios` source is modified.
- Production code must not import mock auth/cards/matches/chat state.
- API errors, refresh retry, offline state and rate limits are explicit UI states.
- Registration/setup/mode behavior matches backend `GET /onboarding`.
- Tokens remain in secure platform storage only.

## Review Focus

- Completed account login goes main; incomplete setup follows `next_setup`.
- One project is represented by manage state, never a second-create action.
- Both platforms serialize identical DTOs and enums.
- Chat/match screens cannot bypass authorization.
- Android/iOS builds pass without test-only mock dependencies.

---

### Task 1: Standardize shared API/repository wiring

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/App.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/auth/AuthRepository.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/network/ApiClient.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: iOS Phases 1–4 APIs and backend error/rate-limit contracts.
- Produces: shared request layer with bearer injection, one refresh retry, timeout, structured error mapping and request IDs.

- [ ] Step 1: Add tests for offline, one refresh retry, 401 cleanup, 403/404/409/429 mapping and timeout.
- [ ] Step 2: Implement shared ApiClient and connect repositories through it.
- [ ] Step 3: Ensure mock services remain test-only and are not wired into production App.
- [ ] Step 4: Run common tests and commit `refactor: standardize mobile API wiring`.

### Task 2: Complete mobile product flows

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/CatalogScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/ProfileScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/FavoritesScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/MatchesScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/NotificationsScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/ChatsScreen.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: iOS Phases 1–4 repositories and routes.
- Produces: real profile, project, favorites, matching, notification and chat state.

- [ ] Step 1: Add tests for loading/error/empty states and navigation after auth refresh.
- [ ] Step 2: Replace remember-only/local production state with repository-backed state and explicit refresh.
- [ ] Step 3: Keep one-project manage state and onboarding mode gates consistent across screens.
- [ ] Step 4: Connect notification badge/read state and match navigation.
- [ ] Step 5: Connect chat history/live/reconnect state.
- [ ] Step 6: Run common, Android and iOS-compatible tests/builds and commit `feat: complete mobile product flows`.

### Task 3: Run mobile acceptance and hand off

**Files:**
- Modify: `frontend/ios/README.md`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/ChatApiTest.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/ChatScreenTest.kt`

**Interfaces:**
- Consumes: Backend acceptance checklist and iOS Phases 1–4.
- Produces: mobile release checklist and acceptance evidence.

- [ ] Step 1: Run common tests, Android build/tests and iOS-compatible compilation/tests.
- [ ] Step 2: Run two-account flow with Mailpit: developer registration, project registration, verification, setup and login-to-main behavior.
- [ ] Step 3: Verify one project per account, developer↔project match, notifications and authorized chat.
- [ ] Step 4: Verify invalid match types and second-project action are unavailable.
- [ ] Step 5: Search production source for mock imports, backend monorepo paths and token-bearing URLs.
- [ ] Step 6: Document environment/build requirements and send results to Backend/Web owners; commit `docs: complete mobile acceptance handoff`.

## Exit Criteria

- Android/iOS use real APIs and secure tokens.
- Onboarding, modes, one-project rule and matching invariants match backend.
- Full mobile registration-to-chat flow passes.

## Handoff

Send platform build versions, API base URL settings, test results and known skips to Backend Phase 5 and Web Phase 5.

## Rollback

Keep production wiring behind repository-level feature boundaries if a platform build fails; never return to plaintext token storage or local production matches.
