# iOS Phase 2 — Onboarding, cards, project mode and favorites implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace local mobile cards with backend profiles/projects, onboarding routing, two discovery modes and private favorites.

**Architecture:** `Find Projects` uses the developer profile; `Find Developers` uses the user’s single project. Setup routing uses the backend onboarding state. Completed login goes to the main page; only incomplete initial setup opens a setup screen.

**Tech Stack:** Kotlin Multiplatform, Compose, Ktor, kotlinx.serialization, commonTest.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`; backend contract: `backend/docs/plans/phase-2-cards-catalog.md`.

## Global Constraints

- One personal profile and zero or one project per account.
- No second-project creation action after a project exists.
- `developer|project` is onboarding intent, not a permanent account role.
- Public cards are published/active API data only.
- Every screen has loading, empty, error and retry states.

## Review Focus

- Completed accounts route to main, not setup.
- Missing profile/project disables only the dependent mode.
- Developer mode cannot send project-owner actions.
- Project summary replaces create-project UI after creation.
- Serialization matches backend DTOs on Android and iOS.

---

### Task 1: Add onboarding and card API models

**Files:**
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/network/CardsApi.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/network/OnboardingApi.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/model/CardModels.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: Backend Phase 2 onboarding/catalog/favorites DTOs.
- Produces: typed onboarding, profile/project, pagination and favorite methods.

- [ ] Step 1: Add serialization tests for onboarding, profile, project, visibility and favorite responses.
- [ ] Step 2: Implement `GET /onboarding` with `mode`, completion, `next_setup` and `can_create_project`.
- [ ] Step 3: Implement profile/project catalog queries with filters and offset/limit.
- [ ] Step 4: Implement single project create/update and map `409` to a safe stale/direct-request error.
- [ ] Step 5: Implement favorite list/save/remove methods.
- [ ] Step 6: Run common tests and commit `feat: add mobile cards and onboarding APIs`.

### Task 2: Implement setup routing and mode gates

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/App.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/RegisterScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/CreateProfileScreen.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/CreateProjectScreen.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: Task 1 onboarding API and iOS Phase 1 auth state.
- Produces: route decision `profile|project|none` and two discovery-mode gates.

- [ ] Step 1: Add tests for incomplete developer setup, incomplete project setup and completed-account main route.
- [ ] Step 2: Fetch onboarding after verified login/session restore.
- [ ] Step 3: Route `next_setup=profile/project` to the correct form; route `none` to main.
- [ ] Step 4: Keep the alternate card action available from the account/project area after initial setup.
- [ ] Step 5: Hide project-create action when `can_create_project=false`; render project summary/manage state.
- [ ] Step 6: Gate `Find Projects` on a usable developer profile and `Find Developers` on an owned project.
- [ ] Step 7: Run common tests and commit `feat: add mobile onboarding and mode gates`.

### Task 3: Replace catalog and favorites screens

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/CatalogScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/FavoritesScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/ProfileScreen.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/ProjectsScreen.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: Tasks 1–2 APIs and state gates.
- Produces: API-backed people/projects screens with persistent favorites.

- [ ] Step 1: Add tests for loading, error, empty, filter and pagination states.
- [ ] Step 2: Replace hardcoded cards/local FavoriteItem state with backend IDs and DTOs.
- [ ] Step 3: Render `Find Projects` with developer context and `Find Developers` with project-owner context.
- [ ] Step 4: Connect profile edit and single project create/edit form.
- [ ] Step 5: Connect favorite actions and reload state after mutation.
- [ ] Step 6: Run common tests/platform compilation and commit `feat: connect mobile card screens`.

### Task 4: Verify and hand off

**Files:**
- Modify: `frontend/ios/README.md`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/SharedCommonTest.kt`

**Interfaces:**
- Consumes: Tasks 1–3 and backend API test output.
- Produces: mobile Phase 2 acceptance evidence for matching.

- [ ] Step 1: Verify completed login routes main and incomplete setup routes correctly.
- [ ] Step 2: Verify one project create flow replaces create action with manage state.
- [ ] Step 3: Verify second project direct/stale request displays safe error and no duplicate card.
- [ ] Step 4: Verify favorites persist across restart.
- [ ] Step 5: Document DTO/route mapping and send it to backend/Web owners; commit `docs: hand off mobile card integration`.

## Exit Criteria

- Mobile catalogs use real profile/project APIs.
- Setup routing follows `next_setup`.
- One project per account is clear in UI and safe against stale requests.
- Favorites persist and never create matches.

## Handoff

iOS Phase 3 consumes the actor mode/context state for matching. Backend receives serialization and mode-gating test evidence.

## Rollback

Keep the existing screen adapters if the API integration is unstable; do not restore production hardcoded cards or local favorites.
