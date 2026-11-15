# Web Phase 3 — Matching and notifications implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect web like/pass actions, match results and private in-app notifications to the backend developer↔project matching contract.

**Architecture:** `Find Projects` sends developer-context swipes; `Find Developers` sends project-owner-context swipes for the user’s single project. The UI never offers developer↔developer or project↔project matching.

**Tech Stack:** React, TypeScript, Vite, fetch, Vitest, Testing Library.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`; backend contract: `backend/docs/plans/phase-3-matching-notifications.md`.

## Global Constraints

- Only `developer ↔ project` matching is valid.
- Repeated actions are idempotent; no undo/unmatch UI in MVP.
- `MatchRead` and `ChatAccessRead` fields are consumed exactly as documented.
- Notifications are in-app only and scoped to the current user.
- Blocked/closed/unpublished projects are not actionable.

## Review Focus

- Developer mode never sends project-owner swipe requests.
- Project mode never sends developer swipe requests without a project context.
- Mutual-like response does not duplicate cards or matches on retry.
- Notification badge/read state is private and idempotent.
- Unauthorized/409/422/429 responses render recoverable UI states.

---

### Task 1: Implement typed matching and notification clients

**Files:**
- Create: `frontend/web/src/shared/lib/api/matching-api.ts`
- Modify: `frontend/web/src/shared/lib/api/cards-api.ts`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`

**Interfaces:**
- Consumes: Backend Phase 3 swipe, MatchRead and notification DTOs.
- Produces: `swipeDeveloperToProject`, `swipeProjectToDeveloper`, `getMatches`, `getMatch`, `getNotifications`, `getUnreadCount`, `markNotificationRead`, `markAllNotificationsRead`.

- [ ] Step 1: Add failing tests for both swipe directions and exact MatchRead parsing.
- [ ] Step 2: Implement APIs that require explicit actor context and target IDs.
- [ ] Step 3: Map `409` pass-after-match, `403` ownership and `422` validation errors.
- [ ] Step 4: Implement notification list/unread/read API methods.
- [ ] Step 5: Run focused tests and commit `feat: add web matching APIs`.

### Task 2: Connect like/pass controls to actor mode

**Files:**
- Modify: `frontend/web/src/pages/catalog/CatalogPage.tsx`
- Modify: `frontend/web/src/pages/projects/ProjectsPage.tsx`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`

**Interfaces:**
- Consumes: Web Phase 2 mode state and Task 1 APIs.
- Produces: developer→project and project-owner→developer actions only.

- [ ] Step 1: Add failing tests proving developer mode calls `/projects/{id}/my-swipe`.
- [ ] Step 2: Add failing tests proving project mode calls `/projects/{id}/candidates/{user_id}/swipe`.
- [ ] Step 3: Disable like/pass controls when required card/context is missing or project is not open.
- [ ] Step 4: Show mutual-match result once and refresh cards from page zero after mutation.
- [ ] Step 5: Ensure retrying the same action does not duplicate cards or local match state.
- [ ] Step 6: Run UI tests and commit `feat: connect web matching actions`.

### Task 3: Add matches and notification screens

**Files:**
- Create: `frontend/web/src/pages/matches/MatchesPage.tsx`
- Create: `frontend/web/src/pages/notifications/NotificationsPage.tsx`
- Modify: `frontend/web/src/app/router.tsx`
- Test: `frontend/web/src/app/router.test.tsx`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`

**Interfaces:**
- Consumes: Task 1 MatchRead/notification clients.
- Produces: `/matches` and `/notifications` routes with summaries, unread count and independent read actions.

- [ ] Step 1: Add failing route tests for loading, unauthorized redirect, empty matches and empty notifications.
- [ ] Step 2: Render project/candidate summary from MatchRead without private fields.
- [ ] Step 3: Render unread badge and mark-read/read-all actions scoped to the current user.
- [ ] Step 4: Link an authorized match to chat route without embedding tokens in URLs.
- [ ] Step 5: Run router/UI tests and commit `feat: add web matches and notifications`.

### Task 4: Verify and hand off

**Files:**
- Modify: `frontend/web/README.md`
- Test: `frontend/web/src/app/router.test.tsx`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`

**Interfaces:**
- Consumes: Tasks 1–3 and Backend Phase 3 test output.
- Produces: web matching acceptance result for Web Phase 4 chat integration.

- [ ] Step 1: Run web tests, TypeScript build and accessibility checks.
- [ ] Step 2: Verify two separate users produce one developer↔project match and two private notifications.
- [ ] Step 3: Verify developer↔developer and project↔project controls do not exist.
- [ ] Step 4: Document endpoint/client mapping and send mismatch report to backend.
- [ ] Step 5: Commit `docs: hand off web matching integration`.

## Exit Criteria

- Both matching directions use correct actor context.
- Invalid match types cannot be initiated from web.
- Matches and notifications use real backend state.

## Handoff

Web Phase 4 consumes `/matches/{id}/chat-access` and the match route; backend receives test evidence for actor direction and notification privacy.

## Rollback

Disable matching controls behind a UI feature flag while retaining read-only match/notification routes; never invent local matches.
