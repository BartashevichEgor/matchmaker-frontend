# iOS Phase 3 — Matching and notifications implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect mobile like/pass actions, match results and private in-app notifications to the backend developer↔project contract.

**Architecture:** `Find Projects` sends developer-to-project swipes. `Find Developers` sends project-owner-to-developer swipes using the user’s single project. The mobile client never exposes developer↔developer or project↔project actions.

**Tech Stack:** Kotlin Multiplatform, Compose, Ktor, kotlinx.serialization, commonTest.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`; backend contract: `backend/docs/plans/phase-3-matching-notifications.md`.

## Global Constraints

- Only developer↔project matching is valid.
- MatchRead and notification DTOs are consumed exactly as documented.
- Like/pass retries are idempotent; no undo/unmatch UI.
- Notifications are in-app and user-private.
- A missing profile/project context disables the dependent action.

## Review Focus

- Each mode calls the correct backend endpoint.
- No invalid target type can be selected from mobile UI.
- Mutual match result is displayed once on retry.
- Notification read state is scoped to the current user.
- Offline/401/403/409/422/429 states are recoverable.

---

### Task 1: Implement matching and notification APIs

**Files:**
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/network/MatchingApi.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/network/NotificationApi.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/model/MatchModels.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: Backend Phase 3 swipe, MatchRead and Notification DTOs.
- Produces: typed APIs for both swipe directions, matches, unread count and read actions.

- [ ] Step 1: Add serialization tests for MatchRead, ChatAccessRead and notification payloads.
- [ ] Step 2: Implement developer-to-project and project-owner-to-developer swipe calls with explicit context.
- [ ] Step 3: Implement matches list/detail and notification list/unread/read-all methods.
- [ ] Step 4: Map 403/409/422/429 to safe UI errors.
- [ ] Step 5: Run common tests and commit `feat: add mobile matching APIs`.

### Task 2: Connect catalog actions to matching context

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/CatalogScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/ProjectsScreen.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: iOS Phase 2 mode gates and Task 1 APIs.
- Produces: correct like/pass actions for each discovery mode.

- [ ] Step 1: Test developer mode calls `/projects/{id}/my-swipe`.
- [ ] Step 2: Test project mode calls `/projects/{id}/candidates/{user_id}/swipe`.
- [ ] Step 3: Disable actions when profile/project context is unavailable or project is closed/blocked.
- [ ] Step 4: Show one match result and refresh from page zero after mutation.
- [ ] Step 5: Run common tests and commit `feat: connect mobile matching actions`.

### Task 3: Add matches and notifications screens

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/MatchesScreen.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/NotificationsScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/App.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: Task 1 models/API.
- Produces: match list/detail, notification badge/list and read actions.

- [ ] Step 1: Add loading/empty/error tests for matches and notifications.
- [ ] Step 2: Render project/candidate summaries without private fields.
- [ ] Step 3: Render unread count and independent mark-read/read-all actions.
- [ ] Step 4: Link authorized matches to chat without token-bearing URLs.
- [ ] Step 5: Run common tests/platform compilation and commit `feat: add mobile matches and notifications`.

### Task 4: Verify and hand off

**Files:**
- Modify: `frontend/ios/README.md`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: Tasks 1–3 and backend concurrency evidence.
- Produces: mobile matching acceptance evidence for chat.

- [ ] Step 1: Verify two separate users create exactly one developer↔project match.
- [ ] Step 2: Verify developer↔developer/project↔project controls are absent.
- [ ] Step 3: Verify two private match notifications and owner-only project-join notification.
- [ ] Step 4: Document endpoint mapping and send results to backend/Web owners; commit `docs: hand off mobile matching`.

## Exit Criteria

- Mobile matching uses correct actor context and real backend state.
- Invalid match types cannot be initiated.
- Matches and notifications are private and idempotent.

## Handoff

iOS Phase 4 consumes match IDs and ChatAccessRead authorization. Backend receives mobile serialization/endpoint evidence.

## Rollback

Disable like/pass controls while keeping read-only matches/notifications; never create local matches.
