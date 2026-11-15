# iOS Phase 4 — Authorized chat implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add mobile chat history, real-time messages and unread state through the separate chat service.

**Architecture:** Mobile opens chat only from a matched conversation. Ktor calls chat REST endpoints and sends WebSocket authentication as the first JSON message. Access tokens never appear in URLs or logs.

**Tech Stack:** Kotlin Multiplatform, Compose, Ktor, WebSocket, kotlinx.serialization, commonTest.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`; backend contract: `backend/docs/plans/phase-4-chat.md`.

## Global Constraints

- Use REST conversation/history/read endpoints exactly as documented.
- WebSocket first event is `authenticate`; send uses `client_message_id`.
- Recheck authorization on reconnect and send failure/expiry.
- Do not store chat access tokens in URLs or logs.
- Unread count/read state is server-owned.

## Review Focus

- Unauthorized match cannot open a socket or history.
- Reconnect does not duplicate subscriptions or messages.
- Duplicate send retry displays one message.
- Empty/loading/error/offline states are recoverable.
- Android/iOS use identical chat payload serialization.

---

### Task 1: Implement ChatApi and protocol models

**Files:**
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/network/ChatApi.kt`
- Create: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/model/ChatModels.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/ChatApiTest.kt`

**Interfaces:**
- Consumes: Backend Phase 4 REST/WebSocket protocol.
- Produces: `listConversations`, `listMessages`, `sendMessage`, `markRead`, `connectChatSocket`.

- [ ] Step 1: Add serialization tests for conversation, message, unread and error payloads.
- [ ] Step 2: Implement REST calls with bearer access token and cursor pagination.
- [ ] Step 3: Implement WebSocket authenticate/send/read events and acknowledgements.
- [ ] Step 4: Implement reconnect/backoff with a bounded retry policy.
- [ ] Step 5: Run common tests and commit `feat: add mobile chat client`.

### Task 2: Connect ChatsScreen and navigation

**Files:**
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/ChatsScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/MatchesScreen.kt`
- Modify: `frontend/ios/shared/src/commonMain/kotlin/com/example/projectmatchmaker/App.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/ChatScreenTest.kt`

**Interfaces:**
- Consumes: Task 1 client and iOS Phase 3 match IDs.
- Produces: conversation list, history, live send/receive, unread/read UI.

- [ ] Step 1: Add failing tests for authorized match, forbidden match, empty conversation, connection failure and retry.
- [ ] Step 2: Load conversations/history and render server unread counts.
- [ ] Step 3: Open socket only from authorized match context and never put token in navigation arguments.
- [ ] Step 4: Persist/send before showing delivered state; resolve pending messages with ack/duplicate ack.
- [ ] Step 5: Mark visible messages read and expose reconnect controls.
- [ ] Step 6: Run common tests/platform compilation and commit `feat: integrate mobile authorized chat`.

### Task 3: Verify and hand off chat

**Files:**
- Modify: `frontend/ios/README.md`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/ChatApiTest.kt`
- Test: `frontend/ios/shared/src/commonTest/kotlin/com/example/projectmatchmaker/ChatScreenTest.kt`

**Interfaces:**
- Consumes: Backend Phase 4 service and iOS Phase 3 matches.
- Produces: mobile chat acceptance evidence for Phase 5.

- [ ] Step 1: Verify matched candidate/project owner can send, receive and mark read.
- [ ] Step 2: Verify outsider receives forbidden and cannot connect/history-read.
- [ ] Step 3: Verify reconnect/send-after-expiry reauthorization.
- [ ] Step 4: Verify duplicate client IDs do not duplicate messages.
- [ ] Step 5: Document service URL/protocol and send results to backend/Web owners; commit `docs: hand off mobile chat`.

## Exit Criteria

- Mobile chat works only for authorized matched users.
- REST/WebSocket payloads match backend contract.
- Unread state, retries and reconnect are correct.

## Handoff

iOS Phase 5 consumes this ChatApi/ChatsScreen integration. Backend receives mobile protocol test evidence.

## Rollback

Disable mobile chat navigation while retaining match screens; do not bypass FastAPI authorization.
