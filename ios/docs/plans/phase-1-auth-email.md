# iOS Phase 1 — Auth, registration mode and email verification implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect the Kotlin Multiplatform client to backend registration modes, email verification and secure access/refresh sessions.

**Architecture:** Ktor sends mobile JSON token pairs. Access/refresh tokens are stored atomically in secure platform storage; access tokens refresh once after `401`. Registration submits `developer|project`; setup routing is finalized in iOS Phase 2 using the onboarding endpoint.

**Tech Stack:** Kotlin Multiplatform, Ktor, kotlinx.serialization, Android EncryptedSharedPreferences, iOS Keychain, commonTest.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`; backend contract: `backend/docs/plans/phase-1-auth-email.md`.

## Global Constraints

- Registration mode is exactly `developer|project`; no `both` option.
- Refresh tokens never go to logs, URLs or unencrypted storage.
- One `401` triggers one refresh; failed refresh clears both tokens.
- Verification code/link errors are safe and enumeration-resistant.
- Mailpit is used through backend Compose; the mobile app talks only to the API.

## Review Focus

- Android and iOS serialize the same auth DTOs.
- Token writes are atomic and cannot leave a half-updated pair.
- Keychain/encrypted storage survives app restart and logout clears all credentials.
- Mode selection persists across verification and is sent exactly once.
- Completed accounts are not forced into setup by auth code.

---

### Task 1: Update auth models and API

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/network/AuthApi.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/network/AuthModels.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/auth/AuthRepository.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/network/AuthModelsTest.kt`

**Interfaces:**
- Consumes: Backend Phase 1 mobile endpoints and DTOs.
- Produces: register/login/refresh/logout/resend/verify methods with `mode=developer|project`.

- [ ] Step 1: Add failing serialization tests for mode, mobile token pair, session response and verification responses.
- [ ] Step 2: Define `OnboardingMode` enum with only `DEVELOPER` and `PROJECT` wire values.
- [ ] Step 3: Implement AuthApi request/response models for mobile JSON tokens.
- [ ] Step 4: Add verification code/link/resend methods and safe error mapping.
- [ ] Step 5: Run common tests and commit `feat: connect mobile auth contract`.

### Task 2: Secure token lifecycle and registration UI

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/auth/TokenStorage.kt`
- Modify: `frontend/ios/shared/src/androidMain/kotlin/com/example/projectmatchmaker/auth/TokenStorage.android.kt`
- Modify: `frontend/ios/shared/src/iosMain/kotlin/com/example/projectmatchmaker/auth/TokenStorage.ios.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/RegisterScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/VerifyEmailScreen.kt`
- Create: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/AuthRepositoryTest.kt`

**Interfaces:**
- Consumes: Task 1 AuthApi.
- Produces: secure token-pair storage, mode selector and verification UI.

- [ ] Step 1: Add failing tests for atomic token save, one refresh retry, failed-refresh cleanup and logout cleanup.
- [ ] Step 2: Replace single-token storage with atomic access/refresh storage.
- [ ] Step 3: Keep Android EncryptedSharedPreferences and use iOS Keychain instead of NSUserDefaults.
- [ ] Step 4: Add exactly two registration choices and send selected mode with registration.
- [ ] Step 5: Render code/link/resend/expired/SMTP-error states without exposing secrets.
- [ ] Step 6: Run common tests and Android/iOS-compatible compilation; commit `feat: secure mobile auth lifecycle`.

### Task 3: Verify Mailpit/auth handoff

**Files:**
- Modify: `frontend/ios/README.md`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/AuthRepositoryTest.kt`

**Interfaces:**
- Consumes: `backend/docs/mailpit/README.md` and backend auth contract.
- Produces: mobile auth handoff for iOS Phase 2 onboarding.

- [ ] Step 1: Run common tests and platform compilation.
- [ ] Step 2: Test registration with `developer` and `project` modes against backend Mailpit flow.
- [ ] Step 3: Confirm completed login leaves setup routing to the onboarding contract.
- [ ] Step 4: Document mobile API base URL and Mailpit test prerequisites; commit `docs: hand off mobile auth`.

## Exit Criteria

- Mobile submits valid mode and verifies email through backend code/link endpoints.
- Tokens are secure, refresh once and clear correctly.
- Android/iOS use the same DTOs and error behavior.

## Handoff

iOS Phase 2 consumes `GET /api/v1/onboarding` and routes completed accounts to main, incomplete initial setup to the correct form.

## Rollback

Keep the existing AuthRepository adapter if platform storage tests fail; never fall back to plaintext token storage.
