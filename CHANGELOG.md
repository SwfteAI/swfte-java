# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/SwfteAI/swfte-java/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/SwfteAI/swfte-java/releases/tag/v1.1.0
[1.0.0]: https://github.com/SwfteAI/swfte-java/releases/tag/v1.0.0
