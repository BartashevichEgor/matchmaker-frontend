# Web Phase 4 — Authorized chat implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add web chat history, real-time messages and unread state using the separate backend chat service and FastAPI match authorization.

**Architecture:** The web opens chat only from an existing authorized match. REST loads conversation/history; WebSocket sends the access token in the first JSON message, never in the URL. The web client does not make authorization decisions locally.

**Tech Stack:** React, TypeScript, WebSocket, fetch, Vitest, Testing Library.

**Spec:** `docs/superpowers/specs/2026-11-14-matchmaker-mvp.md`; backend contract: `backend/docs/plans/phase-4-chat.md`.

## Global Constraints

- Do not put access tokens in WebSocket URLs, route params or logs.
- Use `GET /chat/conversations`, message history, send and read endpoints exactly as documented.
- WebSocket first event is `authenticate`; send uses `client_message_id` for retries.
- Nonparticipants receive a safe forbidden state and cannot render or send messages.
- Message history uses cursor pagination; unread counts come from the service.

## Review Focus

- Unauthorized match route never opens a socket.
- Reconnect and send-after-expiry are handled without leaking backend details.
- Duplicate send retry renders one message and one acknowledgement.
- Empty/history/loading/error states are accessible and recoverable.
- Read events clear only the current user’s unread state.

---

### Task 1: Implement typed chat REST/WebSocket client

**Files:**
- Create: `frontend/web/src/shared/lib/api/chat-client.ts`
- Test: `frontend/web/src/app/router.test.tsx`

**Interfaces:**
- Consumes: Backend Phase 4 REST and WebSocket payloads.
- Produces: `listConversations`, `listMessages`, `sendMessage`, `markRead`, `connectChatSocket`.

- [ ] Step 1: Add failing tests for conversation/history/read DTOs and error mapping.
- [ ] Step 2: Implement REST requests with bearer access token and cursor/limit handling.
- [ ] Step 3: Implement WebSocket authentication as first JSON message `{type:"authenticate",access_token,match_id}`.
- [ ] Step 4: Implement send/read events, `client_message_id`, acknowledgements, duplicate acknowledgements and safe error codes.
- [ ] Step 5: Close and recreate the socket on authorization expiry or reconnect.
- [ ] Step 6: Run focused tests and commit `feat: add web chat client`.

### Task 2: Implement chat page and route

**Files:**
- Create: `frontend/web/src/pages/chat/ChatPage.tsx`
- Modify: `frontend/web/src/app/router.tsx`
- Modify: `frontend/web/src/pages/matches/MatchesPage.tsx`
- Test: `frontend/web/src/app/router.test.tsx`

**Interfaces:**
- Consumes: Task 1 client and Web Phase 3 match route.
- Produces: `/chat/:matchId` screen with history, send, reconnect and unread behavior.

- [ ] Step 1: Add failing tests for authorized match, forbidden match, empty state, connection failure and retry.
- [ ] Step 2: Load conversation/history using cursor pagination and render server unread count.
- [ ] Step 3: Open socket only after match access is confirmed; never include access token in the URL.
- [ ] Step 4: Persist/send message before rendering it as delivered; use acknowledgement to resolve pending state.
- [ ] Step 5: Mark read when messages are visible and expose retry/reconnect controls.
- [ ] Step 6: Run UI tests/build and commit `feat: integrate authorized web chat`.

### Task 3: Verify chat handoff

**Files:**
- Modify: `frontend/web/README.md`
- Test: `frontend/web/src/app/router.test.tsx`

**Interfaces:**
- Consumes: Backend Phase 4 protocol and Web Phase 3 matches.
- Produces: web chat acceptance evidence for Phase 5.

- [ ] Step 1: Verify matched users can load history, send, receive and mark read.
- [ ] Step 2: Verify outsiders receive `403`/forbidden and cannot connect.
- [ ] Step 3: Verify reconnect and send-after-expiry reauthorize.
- [ ] Step 4: Verify duplicate `client_message_id` does not duplicate messages.
- [ ] Step 5: Document chat environment URL and send results to backend/iOS owners; commit `docs: hand off web chat`.

## Exit Criteria

- Web users can chat only through authorized matches.
- REST and WebSocket flows use the exact backend protocol.
- Tokens never appear in URLs or logs.

## Handoff

Web Phase 5 consumes this chat page/client and removes remaining mock paths. Backend receives protocol test evidence.

## Rollback

Disable the chat route/client integration while keeping match pages available; do not create local messages or bypass authorization.
