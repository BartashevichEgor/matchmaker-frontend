# Web Phase 2 — Onboarding, cards, project mode and favorites implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace static web cards with backend profiles/projects, onboarding redirects, two discovery modes and private favorites.

**Architecture:** The web keeps one account context but exposes two discovery modes: `Find Projects` uses the developer profile; `Find Developers` uses the user’s single project card. A completed login goes to the main page; only incomplete initial setup redirects to setup.

**Tech Stack:** React, TypeScript, Vite, fetch, Vitest, Testing Library.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`; backend contract: `backend/docs/plans/phase-2-cards-catalog.md`.

## Global Constraints

- Public cards come only from API responses; no production mock cards.
- `Find Projects` requires a published developer profile.
- `Find Developers` requires an owned project; if the project exists, show its summary/manage state instead of create-project action.
- Show only the catalog supported by the user's completed card when they have one card type; show the catalog tabs only after both profile and project cards are complete.
- No second-project button is rendered; handle defensive API `409` for stale/direct requests.
- Profile/project card publication is separate from the current discovery mode.
- Use loading, error, empty and retry states for every catalog request.

## Review Focus

- `next_setup=none` routes completed accounts to the main page.
- With only a completed developer profile, show Projects without a catalog switcher; with only a completed project, show Specialists without a switcher; show both tabs when both cards are complete.
- A user cannot accidentally send project-owner actions while browsing projects.
- A second project is not offered by UI and stale `409` is rendered safely.
- Pagination/filter changes do not resurrect hardcoded cards.

---

### Task 1: Add typed catalog/onboarding/favorites clients

**Files:**
- Create: `frontend/web/src/shared/lib/api/cards-api.ts`
- Create: `frontend/web/src/shared/lib/api/favorites-api.ts`
- Create: `frontend/web/src/shared/lib/api/onboarding-api.ts`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`

**Interfaces:**
- Consumes: Backend Phase 2 profile/project/onboarding/favorites DTOs.
- Produces: typed `getOnboarding`, `getProfiles`, `getProjects`, `getMyProject`, `createProject`, `updateProject`, `saveFavorite`, `removeFavorite`.

- [ ] Step 1: Add failing request/response serialization tests for onboarding, profile, project and favorite DTOs.
- [ ] Step 2: Implement onboarding client with `mode`, `next_setup`, `can_create_project`, completion and visibility fields.
- [ ] Step 3: Implement bounded catalog query parameters and API error mapping for `401`, `403`, `409`, `422` and `429`.
- [ ] Step 4: Implement one-project create/update client without a second-project UI assumption.
- [ ] Step 5: Run focused tests and commit `feat: add web card API clients`.

### Task 2: Implement setup redirect and mode gating

**Files:**
- Modify: `frontend/web/src/app/router.tsx`
- Modify: `frontend/web/src/app/App.tsx`
- Modify: `frontend/web/src/pages/register/RegisterPage.tsx`
- Modify: `frontend/web/src/pages/profile/CreateProfilePage.tsx`
- Create: `frontend/web/src/pages/projects/CreateProjectPage.tsx`
- Test: `frontend/web/src/app/router.test.tsx`

**Interfaces:**
- Consumes: Task 1 onboarding client and Web Phase 1 auth state.
- Produces: deterministic route decision: `next_setup=profile` → profile setup, `project` → project setup, `none` → main page.

- [ ] Step 1: Add failing tests for developer-mode incomplete setup, project-mode incomplete setup and completed-account main-page routing.
- [ ] Step 2: Call `GET /api/v1/onboarding` after verified login/session restoration.
- [ ] Step 3: Route only incomplete initial setup to setup pages; do not redirect completed logins away from the main page.
- [ ] Step 4: Keep the alternate setup action available from the account/project area after initial setup.
- [ ] Step 5: Hide project-create action when `can_create_project=false`; show project summary and edit action instead.
- [ ] Step 6: Run router tests and commit `feat: add web onboarding routing`.

### Task 3: Replace catalog and project/profile pages

**Files:**
- Modify: `frontend/web/src/pages/catalog/CatalogPage.tsx`
- Modify: `frontend/web/src/pages/profile/ProfilePage.tsx`
- Create: `frontend/web/src/pages/projects/ProjectsPage.tsx`
- Modify: `frontend/web/src/app/router.tsx`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`

**Interfaces:**
- Consumes: Task 1 clients and Task 2 mode state.
- Produces: API-backed `Find Projects`, `Find Developers`, profile editor and single-project editor.

- [ ] Step 1: Add failing tests for people/projects tabs, filters, pagination, empty state, loading state and API errors.
- [ ] Step 2: Replace hardcoded Alex/TeamFlow cards with typed API responses.
- [ ] Step 3: Map role/skill selectors to `/roles` and `/skills` IDs.
- [ ] Step 4: Render `Find Projects` with developer actions only.
- [ ] Step 5: Render `Find Developers` with selected project-owner context only.
- [ ] Step 6: Render project summary/manage state after creation and never show a second create button.
- [ ] Step 7: Run Vitest/TypeScript build and commit `feat: connect web cards and project mode`.

### Task 4: Connect favorites and hand off

**Files:**
- Modify: `frontend/web/src/pages/catalog/CatalogPage.tsx`
- Create: `frontend/web/src/pages/favorites/FavoritesPage.tsx`
- Test: `frontend/web/src/pages/profile-and-catalog-pages.test.tsx`
- Modify: `frontend/web/README.md`

**Interfaces:**
- Consumes: Task 1 favorite client and Tasks 2–3 card state.
- Produces: private, persistent profile/project favorites with no matching side effects.

- [ ] Step 1: Add failing tests for save/list/remove/duplicate-save behavior.
- [ ] Step 2: Connect bookmark actions to PUT/DELETE favorites and reload saved state from GET favorites.
- [ ] Step 3: Confirm saving own profile/project is rejected by backend and rendered as a safe error.
- [ ] Step 4: Add documentation for modes, setup routing and one-project UI behavior.
- [ ] Step 5: Run web tests/build and send the completed DTO/route checklist to Backend Phase 3; commit `feat: persist web card favorites`.

## Exit Criteria

- Completed accounts enter the main page after login.
- Incomplete initial accounts go to the correct setup form.
- People/projects modes use correct actor context.
- One project per account is enforced visibly and defensively.
- Favorites persist without creating matches.

## Handoff

Web Phase 3 consumes backend matching/notification endpoints and replaces catalog like/pass actions. Backend receives the route/test results and any API mismatch report.

## Rollback

Keep the API clients behind the existing page adapters if a catalog endpoint is unstable; never restore hardcoded data as a production fallback.
