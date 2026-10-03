# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-09-30

First release published to Maven Central. Versions 1.0.x and 1.1.x were never
published; 1.2.0 supersedes them.

### Security

- Jackson 2.15.3 -> 2.18.10 (databind, core, annotations); 2.15.3 carried 11 published advisories.
- **`HttpClient` retry policy.** It used to retry every failure three times, including
  403/404/500 on POST create calls (duplicate creates and billing). Now: a 4xx is never retried
  (401/403 `AuthenticationException`, 429 `RateLimitException` carrying `Retry-After`,
  other 4xx `ApiException`); only connection errors, timeouts and 5xx are retried, and only for
  GET/HEAD or a request sent with an idempotency key (new
  `request(..., idempotencyKey)` / `requestWithCustomBase(..., idempotencyKey)` overloads).
  JSON bodies, including streaming chat and byte-response speech calls, use
  fixed-length streaming; multipart audio and file uploads use chunked streaming.
  Both modes prevent `HttpURLConnection` from re-sending a consumed POST on its
  own, outside the SDK retry loop. Null-body POSTs use zero-length streaming;
  these POST paths refuse automatic redirects. `maxRetries(0)` now makes one attempt instead
  of failing without sending.
- **No request follows a redirect.** GET, DELETE and the other methods used to inherit
  `HttpURLConnection`'s automatic redirect following, which could replay the workspace
  header (and `Authorization` on a same-origin hop) at the redirect target. Every connection
  now disables redirects and any 3xx is an `ApiException` (not retried), matching the Node
  and Python SDKs.
- Release workflow: actions pinned to full commit SHAs, GitHub Release step moved into its own
  job (away from the GPG key and Central token), publish only from `main`, the "already on
  Central" check reads repo1.maven.org (fail closed) instead of the lagging search index.

### Fixed

- `User-Agent` reports the real version (was hard-coded `swfte-java/1.0.0`); the pom is the
  only place it is written (`SdkVersion`).
- README install snippets and PyPI link match the released artifacts.
- Public single and batch file uploads use the agents-service `apiBaseUrl`,
  preserving gateway routing for audio transcription.
- String responses (for example CSV exports) keep their line breaks.
- `postStream` disconnects when the returned `Stream` is closed.
- Compiled with `--release 11` so the jar cannot link newer JDK APIs; `Automatic-Module-Name: com.swfte.sdk`.

## [Unreleased]

### Fixed

- `workflows().invokeAndWait` / `waitForCompletion` no longer burn the whole
  timeout on a human-in-the-loop run: `PAUSED` / `WAITING_FOR_INPUT`
  (`WorkflowExecution.PAUSED_STATUSES`) return at once with `isPaused()` true and
  `getWaitingFor()` naming the gate; the `throwOnPause` overload throws
  `WorkflowPausedException` instead. `WorkflowExecution.Outcome` gains `PAUSED`.
- `AgentChatResponse.getResponse()` prefers the canonical `content` over the
  legacy `response` when a reply carries both.
- `SwfteClient.deriveApiBaseUrl` also strips a bare trailing `/gateway`.

### Added

- `agents().chat(agentId, message[, AgentChatOptions])` —
  `POST /v1/agents/{agentId}/chat/{userId}` with `{"message", "conversationId"?}`.
  Returns `AgentChatResponse` (`getResponse()`, normalised from `content` when the
  server uses that key; `getConversationId()`; `getRaw()`). `userId` defaults to
  `"sdk-user"` (`AgentChatOptions.DEFAULT_USER_ID`).
- `workflows().invoke(workflowId, inputs)` — `POST /v2/workflows/{id}/invoke`,
  runs the published snapshot; returns `WorkflowInvocation` with the execution ID.
- `workflows().invokeAndWait(workflowId, inputs[, timeoutMs, pollIntervalMs])` —
  invokes and polls to a terminal status. Success is any of `SUCCESS`,
  `SUCCEEDED`, `COMPLETED`; `FAILED`/`TIMEOUT`/`CANCELLED`/`CANCELED` throw
  `WorkflowExecutionException`; the deadline throws `WorkflowTimeoutException`.
- `catalog().search(CatalogSearchParams)`, `catalog().get(kind, id)`,
  `catalog().contract(kind, id)` over `/v2/catalog/*`, with typed
  `CatalogSearchResponse`, `CatalogEntry` and `CatalogContract`.
- `Builder.apiBaseUrl(...)` (and `SWFTE_API_BASE_URL`) for the agents-service root;
  defaults to `baseUrl` minus its trailing gateway segment, which is what the
  management resources already computed. `SwfteClient.getApiBaseUrl()`.
- `HttpClient.apiRequest(...)`: single attempt, fixed-length body (so
  `HttpURLConnection` cannot silently re-send a POST), 401/403 ->
  `AuthenticationException`, 429 -> `RateLimitException`, else `ApiException`
  with `getResponseBody()`.

### Changed

- `workflows().getExecutionStatus()` parses the server's
  `{"execution": {...}, "nodeExecutions": [...], "progress": n}` shape. It used to
  bind the top level directly, so `getStatus()` was always `null` (and a
  `SUCCESS` status would not have parsed into the enum anyway).
  `WorkflowExecution.Status` gains `SUCCESS`, `SUCCEEDED`, `TIMEOUT`, `CANCELED`;
  new `getStatusRaw()`, `getOutcome()`, `isTerminal()`, `isSucceeded()`,
  `getNodeExecutions()`, `getRaw()`.
- `workflows().waitForCompletion(...)` shares the new terminal rules (it only
  knew `COMPLETED`, so it timed out on every successful run) and throws the new
  exceptions, both `RuntimeException`s as before.

### Fixed

- `SwfteClientTest` asserted the pre-1.1.1 default URL.

## [1.1.1] - 2026-09-01

### Fixed

- **The shipped default `baseUrl` returned 403.** `SwfteClient.builder().apiKey(...)`
  could not make a request: the default pointed at
  `https://api.swfte.com/v2/gateway`, but the gateway lives behind `/agents`, so
  the call was refused with a bare nginx 403. Corrected to
  `https://api.swfte.com/agents/v2/gateway` and verified live against production.

### Changed

- **Publishing moved from OSSRH to the Sonatype Central Portal.** The previous
  configuration deployed to `s01.oss.sonatype.org`, which Sonatype has retired —
  it now answers 404, so no release could ever have succeeded from it. The
  `release` profile uses `central-publishing-maven-plugin` with
  `autoPublish=false`, leaving a validated bundle in the Portal for a human to
  publish, because a Maven Central coordinate cannot be recalled.


## [1.1.0] - 2026-05-07

### Added
- Coverage for the top 15 V2 controllers of the Swfte agents-service backend.
- New resource clients: `ChatFlows`, `AgentWizard`, `Datasets`, `Documents`, `Files`, `Rag`, `Mcp`, `Modules`, `Marketplace`, `VoiceCalls`, `Audit`, `CostControl` — exposed from `SwfteClient` via `client.chatflows()`, `client.agentWizard()`, `client.datasets()`, `client.documents()`, `client.files()`, `client.rag()`, `client.mcp()`, `client.modules()`, `client.marketplace()`, `client.voiceCalls()`, `client.audit()`, `client.costControl()`.
- New model classes: `ChatFlow`, `ChatFlowSession`, `ChatFlowVersion`, `Dataset`, `Document`, `FileMetadata`, `MCPServer`, `MCPTool`, `Module`, `ModuleVersion`, `Publication`, `Installation`, `VoiceCall`, `AuditEvent`, `RoutingRule`, `UsageCap`, `RagSearchRequest`, `RagSearchResponse`.
- `ABOUT.md` company profile and an "About Swfte" section in the README.
- `docs/cookbook/` — runnable Java examples for each top-15 V2 controller.
- Unit tests for every new resource (instantiation + URL/method assertions via stubbed HTTP client).

### Changed
- README "Documentation" link now points to `swfte.com/developers` and the new cookbook.
- `<scm>` and project `<url>` in `pom.xml` corrected to `https://github.com/SwfteAI/swfte-java` (mixed-case org).
- Maven version bumped to `1.1.0`.

### Compatibility
- Backwards-compatible. All existing 1.0.0 resource methods are unchanged.

## [1.0.0] - 2025-01-XX

### Added
- Unified API client for all AI providers
- Chat completions with streaming support
- Image generation (DALL-E, Stable Diffusion)
- Audio transcription and text-to-speech
- Embeddings generation
- Agent management (CRUD operations)
- Workflow orchestration
- Automatic retry logic with exponential backoff
- Rate limit handling
- Lombok-based builders for all models
- Java 11+ support (compatible with 17 and 21)
- Maven Central publishing

### Supported Providers
- OpenAI (GPT-4, GPT-3.5, DALL-E, Whisper, TTS)
- Anthropic (Claude 3 family)
- Google (Gemini Pro)
- Self-hosted models via RunPod

---

[Unreleased]: https://github.com/SwfteAI/swfte-java/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/SwfteAI/swfte-java/releases/tag/v1.2.0
[1.1.0]: https://github.com/SwfteAI/swfte-java/releases/tag/v1.1.0
[1.0.0]: https://github.com/SwfteAI/swfte-java/releases/tag/v1.0.0
