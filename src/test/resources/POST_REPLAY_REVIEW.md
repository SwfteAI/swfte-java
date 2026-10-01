# Java POST repair source handoff

Status: SOURCE FROZEN FOR DRIVER VERIFICATION. The repair agent executed no
build, install, test, mutation or network command. This report is in the final
source checkpoint; its exact commit is supplied separately in the handoff.

Base: `e35e45f548eef763cee2d76a507fb00a0f1e1a04`.
Early production checkpoint: `250956a` (three narrow native transport guards).

Changes and source evidence:

- `HttpClient.postStream` and `postBytes` serialize the existing JSON into UTF-8
  bytes, set fixed-length streaming before opening output, and write those
  exact bytes. This removes HttpURLConnection's replayable buffered POST.
- `postMultipart` sends existing fields incrementally with chunked streaming.
  It does not prebuffer uploaded audio/files or change multipart field syntax.
- The existing gateway/custom JSON loop and single API request already used
  fixed-length writes for non-null bodies. Null-body wire POSTs, including a
  PATCH represented by POST with an override header, now use zero-length
  streaming, so a side-effecting empty request cannot be buffered for replay.
- Public `Files.upload` and `uploadBatch` use a narrow
  `postMultipartWithCustomBase` helper targeting `apiBaseUrl`. Gateway audio
  transcription still calls `postMultipart` targeting `baseUrl`. Common
  authentication, workspace and multipart behavior is shared.
- PR CI explicitly requests `contents: read`. Existing release action pins,
  signing environment, main guard and separate GitHub Release job are unchanged.
  Retry/routing/redirect documentation now describes these POST paths.
- All source POST entry points were traced to execute/requestWithCustomBase,
  apiRequest, postStream, postMultipart/postMultipartWithCustomBase or postBytes.
  No separate HTTP transport or native retry loop exists in resource classes.
  Explicit idempotency-key retry semantics are preserved; API invocation,
  streaming, multipart and speech helpers do not add SDK retries.

Native proof source:

`src/test/java/com/swfte/sdk/unit/PostHelperReplayTest.java` has 16 named tests.
Real HttpServer instances record a fully consumed request before returning no
response, normal JSON/SSE/binary output, or cross-origin 302/307. Tests require
the default native buffered-POST retry path to remain enabled; do not disable
`sun.net.http.retryPost` to manufacture a green result.

The methods cover stream mode directly, transcription, single and batch files,
both speech overloads, gateway/custom/API central JSON helpers and null-body
POSTs. They assert one actual POST, complete UTF-8/multipart bytes, auth and
workspace headers and normal decoder behavior. Cross-origin sink servers record
every GET/POST, so a changed-method 302 and body-preserving 307 are both visible.
The file-route test uses separate gateway/API servers for each success and
consumed-body disconnect case; gateway hits must be zero.

Exact initial driver selection, in this clone with Java21:

```text
mvn -B test -Dtest=PostHelperReplayTest,HttpClientRetryTest,ReleaseShapeTest
mvn -B verify
python3 /private/tmp/swfte-p5-resume-20261001/.unlazy/p5-code/tools/java-mutants.py src/test/resources/post-replay-mutations.json
```

The gate ledger contains every fine-grained method selector. Require all 16
PostHelperReplayTest methods and the CI permission method to execute with zero
failures/errors/skips. Full existing local-unit compatibility and jar release11
checks remain mandatory. Never substitute BUILD SUCCESS for those XML counts.

Literal guard matrix: `src/test/resources/post-replay-mutations.json`.
Its 17 mutants remove each special streaming guard, central JSON/empty POST
guard, or route files back through the gateway. Distinct redirect selectors
prove the sink boundary separately from response-EOF replay. Every exact
replacement anchor was inspected and matched once; no mutation was run.

The shared driver runner requires a fresh compiled Java21 baseline and records
baseline/killed/restored runs with byte restoration. Additionally require
mutant errors=0, skipped=0, exactly the baseline/restored named-test set, and the
matrix's named assertion in fresh XML. Its current generic killed predicate
alone does not enforce all of those requirements; root has been informed.
Compilation/import/setup failures must not count as killed guards.

Four source passes:

1. Repair all three buffered helper paths without changing request payloads.
2. Trace every public POST transport, preserve explicit idempotency and gateway
   semantics, and inspect the real API-root file contract.
3. Add the uncovered null-body POST protection and source/sink/normal controls;
   inspect direct stream behavior, binary fields, server cleanup and literal
   mutation anchors.
4. Re-read all source, named tests, docs and CI permissions against ownership
   and current requirements. No further source defect remained in this pass.

Remaining owner work: run the Java21 native baseline, full package verification
and all 17 baseline/killed/restored controls with strict fresh XML inspection;
independently assess source/tip and expose the authorized release PR. Streaming
mode compatibility with the real remote upload service remains a release
integration check; local native controls establish request integrity, not
production availability. No runtime pass or release readiness is claimed.
