# Web Phase 1 — Auth, registration mode and email verification implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect the standalone web client to the backend auth contract, two-choice registration, Mailpit/SMTP verification and secure web session lifecycle.

**Architecture:** Keep the access token in memory and use the backend HttpOnly refresh cookie. Registration collects `developer|project`; the selected mode is sent to the backend and used later by the Phase 2 onboarding endpoint. A completed account goes to the main page on normal login.

**Tech Stack:** React, TypeScript, Vite, fetch, Vitest, Testing Library.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`; backend contract: `backend/docs/plans/phase-1-auth-email.md`.

## Global Constraints

- Use only `frontend/` as source; do not import backend `frontend/` files.
- Registration mode is exactly `developer|project`; never send `both`.
- Refresh token is never stored in localStorage or exposed to JavaScript.
- One 401 causes at most one refresh attempt and one original-request retry.
- Verification code/link secrets never appear in logs or URLs except the user-facing verification link.

## Review Focus

- Refresh failure clears memory state and never loops.
- Registration rejects invalid mode before sending the request.
- Verification resend and link/code flows show safe errors without account enumeration.
- A completed user logs in to the main page, not setup.
- Mailpit instructions and visible sender/recipient match the backend contract.

---

### Task 1: Implement typed auth API and secure token lifecycle

**Files:**
- Modify: `frontend/web/src/shared/lib/auth/api-auth-service.ts`
- Modify: `frontend/web/src/shared/lib/auth/auth-types.ts`
- Modify: `frontend/web/src/shared/lib/auth/auth-schemas.ts`
- Modify: `frontend/web/src/app/providers/AuthProvider.tsx`
- Test: `frontend/web/src/shared/lib/auth/api-auth-service.test.ts`
- Test: `frontend/web/src/app/providers/AuthProvider.test.tsx`

**Interfaces:**
- Consumes: Backend Phase 1 web endpoints and DTOs.
- Produces: `AuthService.login`, `AuthService.register({username,email,password,mode})`, `refresh`, `logout`, `resendVerification`, `verifyEmailLink`, `session`.

- [ ] Step 1: Add failing tests for web login, mode payload, cookie credentials, refresh retry, refresh failure cleanup and logout.
- [ ] Step 2: Define TypeScript DTOs for `mode=developer|project`, web tokens, current user and verification responses.
- [ ] Step 3: Replace localStorage token-pair persistence with in-memory access-token state and `credentials: "include"`.
- [ ] Step 4: Centralize authenticated requests so one `401` refreshes once and retries once.
- [ ] Step 5: Add resend/code/link verification methods and safe error mapping.
- [ ] Step 6: Run focused Vitest and commit `feat: connect web auth lifecycle`.

### Task 2: Implement registration and verification screens

**Files:**
- Modify: `frontend/web/src/pages/register/RegisterPage.tsx`
- Modify: `frontend/web/src/pages/verify-email/VerifyEmailPage.tsx`
- Modify: `frontend/web/src/app/router.tsx`
- Test: `frontend/web/src/pages/auth-pages.test.tsx`

**Interfaces:**
- Consumes: Task 1 auth service and Mailpit/SMTP verification contract.
- Produces: registration mode selector, code/link verification, resend action and post-verification auth state.

- [ ] Step 1: Add failing tests for exactly two mode choices and no `both` option.
- [ ] Step 2: Send selected mode with registration and preserve it through the verification transition.
- [ ] Step 3: Render code entry, verification-link success, resend, expired-code and retryable SMTP error states.
- [ ] Step 4: Route successful verification to login/onboarding state without forcing setup for an already completed account.
- [ ] Step 5: Verify accessible labels, keyboard focus and no secret logging.
- [ ] Step 6: Run auth page tests and commit `feat: add web registration mode and verification UI`.

### Task 3: Verify the web auth handoff

**Files:**
- Modify: `frontend/web/README.md`
- Test: `frontend/web/src/pages/auth-pages.test.tsx`
- Test: `frontend/web/src/shared/lib/auth/api-auth-service.test.ts`

**Interfaces:**
- Consumes: Tasks 1–2 and `backend/docs/mailpit/README.md`.
- Produces: documented web auth setup for Backend Phase 2 onboarding.

- [ ] Step 1: Run `npm test`, TypeScript build and focused auth tests.
- [ ] Step 2: Run the Mailpit registration flow with both `developer` and `project` modes.
- [ ] Step 3: Confirm sender `noreply@matchmaker.local`, recipient filtering and code/link verification.
- [ ] Step 4: Confirm completed login does not redirect to setup; leave onboarding routing to Web Phase 2.
- [ ] Step 5: Commit `docs: hand off web auth contract` and send API expectations to the backend/iOS owners.

## Exit Criteria

- Web registration submits exactly `developer|project`.
- Access token is memory-only and refresh uses HttpOnly cookie.
- Verification supports code/link/resend states.
- Refresh failure does not loop or retain stale credentials.

## Handoff

Backend Phase 1 supplies auth/verification behavior. Web Phase 2 consumes `GET /api/v1/onboarding` to decide setup versus main-page routing.

## Rollback

Keep the existing auth service adapter and old endpoints available behind a feature flag if the new cookie flow fails; never reintroduce persistent access tokens.
